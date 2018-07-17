package com.meituan.dataapp.gbdt

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import org.apache.spark.Logging
import scala.collection.mutable.ListBuffer
import scala.math._
import scala.util.Random
import scala.util.control._

class RegressionTree(val root: Node) extends Serializable {
  def predict(d: DataTuple): Double = {
    RegressionTree.predict(d, root)
  }
}

object RegressionTree extends Serializable with Logging {

  def buildRegressionTree(data: RDD[DataTuple], conf: Config): (RegressionTree, ModelStat) = {
    val f = fit(data, conf)
    (new RegressionTree(f._1), f._2)
  }
  
  def predict(d: DataTuple, root: Node): Double = {
    if (root.isLeaf) {
      root.pred
    } else {
      if (d.feature(root.index) == DataTuple.UNKNOWN_VALUE) {
        root.unNode match {
          case Some(n) => predict(d, n)
          case None => root.pred
        }
      } else if (d.feature(root.index) < root.value) {
        root.ltNode match {
          case Some(n) => predict(d, n)
          case None => root.pred
        }
      } else {
        root.geNode match {
          case Some(n) => predict(d, n)
          case None => root.pred
        }
      }
    }
  }

  def almostEqual(v1: Double, v2: Double): Boolean = {
    if (abs(v1 - v2) < 1.0e-4) {
      true
    } else {
      false
    }
  }

  def calcVar(s: Double, ss: Double, c: Double): Double = {
    val fitness0 = {
      if (c > 1) {
        ss - s * s / c
      } else {
        0.0
      }
    }
    if (fitness0 < 0.0) {
      0.0
    } else {
      fitness0
    }
  }

  def squared(v: Double): Double = {
    v * v
  }

  def saveAux(root: Option[Node]): List[Node] = {
    root match {
      case Some(n) => (n :: saveAux(n.ltNode)) ::: saveAux(n.geNode) ::: saveAux(n.unNode)
      case None => List()
    }
  }

  def save(tree: RegressionTree): String = {
    val nodes = RegressionTree.saveAux(Some(tree.root))
    val builder = new StringBuilder()
    for (n <- nodes) {
      if (n != nodes(0)) {
        builder.append("\n")
      }
      builder.append(n.index)
      builder.append(" ")
      builder.append(n.value)
      builder.append(" ")
      builder.append(n.isLeaf)
      builder.append(" ")
      builder.append(n.pred)
      builder.append(" ")
      n.ltNode match {
        case Some(c) => builder.append(nodes.indexOf(c))
        case None => builder.append(0)
      }
      builder.append(" ")
      n.geNode match {
        case Some(c) => builder.append(nodes.indexOf(c))
        case None => builder.append(0)
      }
      builder.append(" ")
      n.unNode match {
        case Some(c) => builder.append(nodes.indexOf(c))
        case None => builder.append(0)
      }
    }
    builder.toString
  }

  def load(s: String): RegressionTree = {
    val nodes = s.split("\n").map(l => {
      val tokens = l.split(" ")
      val index = tokens(0).toInt
      val value = tokens(1).toDouble
      val isLeaf = tokens(2).toBoolean
      val pred = tokens(3).toDouble
      val ltIdx = tokens(4).toInt
      val geIdx = tokens(5).toInt
      val unIdx = tokens(6).toInt
      val n = new Node()
      n.index = index
      n.value = value
      n.isLeaf = isLeaf
      n.pred = pred
      (n, ltIdx, geIdx, unIdx)
    })

    for (i <- nodes) {
      i._1.ltNode = {
        if (i._2 > 0) {
          Some(nodes(i._2)._1)
        } else {
          None
        }
      }
      i._1.geNode = {
        if (i._3 > 0) {
          Some(nodes(i._3)._1)
        } else {
          None
        }
      }
      i._1.unNode = {
        if (i._4 > 0) {
          Some(nodes(i._4)._1)
        } else {
          None
        }
      }
    }

    new RegressionTree(nodes(0)._1)
  }

  //---------------------------------------------------------
  // new way to fit a regression tree in a parallelized way
  //---------------------------------------------------------
  def fit(data: RDD[DataTuple], config: Config): (Node, ModelStat) = {
    val numOfNodes = calcNumOfNodes(config.maxDepth)
    val nodes = new Array[Node](numOfNodes)
    for (i <- 0 until numOfNodes) {
      nodes(i) = new Node
    }
    
    val maxFeatures = {
      val t = (config.numberOfFeature * config.featureSampleRatio + 1).toInt
      if (t < config.numberOfFeature) {
        t
      } else {
        config.numberOfFeature
      }
    }
    
    logInfo("features to be sampled = " + maxFeatures)
    
    val modelStat = new ModelStat(config.numberOfFeature)

    val loop = new Breaks
    loop.breakable {
      for (i <- 0 to config.maxDepth) {
        // fit each layer
        val featureIdx = Random.shuffle((0 until config.numberOfFeature).toList).slice(0, maxFeatures)

        val tt = data.flatMap(d => {
          val n = locateNode(d, nodes, i)
          if (n >= 0) {
            val x = Array.ofDim[Histogram](config.numberOfFeature)
            for (i <- 0 until config.numberOfFeature) {
              x(i) = Histogram(Array.ofDim[Bin](config.maxBins))
            }
            for (i <- featureIdx) {
              val h = Array.ofDim[Bin](config.maxBins)
              h(0) = Bin(
                d.feature(i),
                d.weight,
                d.target * d.weight,
                squared(d.target) * d.weight,
                (2.0 * abs(d.target) - d.target * d.target) * d.weight)
              x(i) = Histogram(h)
            }
            List(Map(n -> x))
          } else {
            List()
          }
        })

        try {
          val hh = tt.reduce(mergeBinGroup(_, _, config.numberOfFeature, config.maxBins))

          // find the best split for each leaf
          val keys = hh.keySet
          for (k <- keys) {
            val pred = config.loss.optimal2(hh(k)(featureIdx.head).histo)
            nodes(k).pred = pred
            if (i < config.maxDepth) {
              val split = Histogram.findBestSplit(hh(k))
              if (split._3 != Double.MaxValue) {
                nodes(k).index = split._1
                nodes(k).isLeaf = true
                nodes(k).value = split._2
                val geEmpty = hh(k)(split._1).histo.filter(bb => bb != null && bb.center >= split._2).isEmpty
                val ltEmpty = hh(k)(split._1).histo.filter(bb => bb != null
                  && bb.center < split._2
                  && bb.center != DataTuple.UNKNOWN_VALUE).isEmpty
                val unEmpty = hh(k)(split._1).histo.filter(bb => bb != null && bb.center == DataTuple.UNKNOWN_VALUE).isEmpty
                if (!geEmpty && !ltEmpty) {
                  nodes(k).geNode = Some(nodes(getLeftChild(k)))
                  nodes(k).ltNode = Some(nodes(getRightChild(k)))
                  if (!unEmpty) {
                    nodes(k).unNode = Some(nodes(getMidChild(k)))
                  }
                  nodes(k).isLeaf = false

                  modelStat.add(split._1, split._4)
                }
              }  // if it can be split
            }  // if it's not the last layer
          }  // for all nodes
        } catch {
          case e: Exception => {
            logWarning(e.getStackTraceString)
            logWarning("raw bin group size = " + tt.count + ", depth = " + i)
            loop.break
          }
        }
      }
    }
    (nodes(0), modelStat)
  }

  def locateNode(d: DataTuple, nodes: Array[Node], level: Int): Int = {
    if (level == 0) {
      return 0
    }

    val n = locateNodeAux(d, Some(nodes(0)), level)
    if (n == null) {
      -1
    } else {
      nodes.indexOf(n)
    }
  }

  def locateNodeAux(d: DataTuple, root: Option[Node], level: Int): Node = {
    root match {
      case Some(n) => {
        if (level == 0) {
          n
        } else if (!n.isLeaf) {
          if (d.feature(n.index) == DataTuple.UNKNOWN_VALUE) {
            locateNodeAux(d, n.unNode, level-1)
          } else if (d.feature(n.index) >= n.value) {
            locateNodeAux(d, n.geNode, level-1)
          } else {
            locateNodeAux(d, n.ltNode, level-1)
          }
        } else {
          null
        }
      }
      case None => null
    }
  }

  def mergeBinGroup(
    m1: Map[Int, Array[Histogram]],
    m2: Map[Int, Array[Histogram]],
    f: Int,
    s: Int): Map[Int, Array[Histogram]] = {
    val keys = m1.keySet ++ m2.keySet
    var o:Map[Int, Array[Histogram]] = Map()
    for (k <- keys) {
      if (!m1.contains(k)) {
        o += (k -> m2(k))
      } else if (!m2.contains(k)) {
        o += (k -> m1(k))
      } else {
        val x = Array.ofDim[Histogram](f)
        for (i <- 0 until f) {
          x(i) = Histogram.merge(m1(k)(i), m2(k)(i), s)
        }
        o += (k -> x)
      }
    }
    o
  }

  def calcNumOfNodes(maxDepth: Int): Int = {
    if (maxDepth == 0) {
      1
    } else {
      calcNumOfNodes(maxDepth-1) * 3 + 1
    }
  }

  def calcNumOfNodesInLevel(level: Int): Int = {
    if (level == 0) {
      1
    } else {
      calcNumOfNodesInLevel(level - 1) * 3
    }
  }

  def getParent(i: Int): Int = {
    (i-1)/3
  }

  def getLeftChild(i: Int): Int = {
    3*i + 1
  }
  def getMidChild(i: Int): Int = {
    3*i + 2
  }
  def getRightChild(i: Int): Int = {
    3*i + 3
  }
}
