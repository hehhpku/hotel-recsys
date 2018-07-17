package com.meituan.hbdata.rec.feature.modelTraining

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/24/15
 */
abstract class ITrainMdoel {
  def genSample(args: Array[String]): Unit

  def prepareTrainSet(args: Array[String]): Unit

  def trainModel(args: Array[String]): Unit

  def evaluateMode(args: Array[String]): Unit
}
