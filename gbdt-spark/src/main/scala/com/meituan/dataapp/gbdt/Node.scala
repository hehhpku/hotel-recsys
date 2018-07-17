package com.meituan.dataapp.gbdt

class Node extends Serializable {
  var geNode: Option[Node] = None
  var ltNode: Option[Node] = None
  var unNode: Option[Node] = None
  var index: Int = -1
  var value: Double = 0
  var isLeaf: Boolean = true
  var pred: Double = 0
}