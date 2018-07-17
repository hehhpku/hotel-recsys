package com.metiuan.dataapp.gbdt

import org.scalatest.FunSuite

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import com.meituan.dataapp.gbdt._

import scala.math._
import scala.io.Source.fromInputStream

class RegressionTreeTest extends FunSuite {
  def loadDataTuple(input: String): List[DataTuple] = {
    val reader = fromInputStream(getClass.getResourceAsStream(input)).bufferedReader
    var x:List[String] = List()
    var line:String = reader.readLine()
    while(line != null) {
      x ::= line
      line = reader.readLine()
    }

    x.map(DataTuple.parse(_: String, 3, false, false))
  }

  test("Regression Tree Local Test") {
    val conf = new SparkConf().setAppName("TestSpark").setMaster("local")
    val sc = new SparkContext(conf)
    val gbrtConf = new Config(
      3,
      4,
      50,
      0.1,
      1,
      0.5,
      0,
      false,
      true,
      new SquaredError,
      10)
    val data = sc.parallelize(loadDataTuple("/train.txt")).map(d => {
      d.target = d.label
      d
    })
    // data.foreach(println(_))

    val r1 = RegressionTree.buildRegressionTree(data, gbrtConf)
    val t = r1._1

    println(RegressionTree.save(t))
    println(r1._2.toString)

    val test = sc.parallelize(loadDataTuple("/test.txt"))
    val scoreAndLabels = test.map(d => (t.predict(d), d.label))
    // scoreAndLabels.foreach(println(_))
    println("RMSE: " + gbrtConf.loss.metrics(scoreAndLabels))

    val r2 = GBDT.buildGBDT(data, gbrtConf)
    val g = r2._1
    val scoreAndLabels2 = test.map(d => (g.predict(d), d.label))
    println("RMSE: " + gbrtConf.loss.metrics(scoreAndLabels2))
    
    println(r2._2.toString)

    sc.stop
  }
}
