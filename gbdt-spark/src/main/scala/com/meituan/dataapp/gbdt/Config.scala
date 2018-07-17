package com.meituan.dataapp.gbdt

case class Config(
  numberOfFeature: Int,
  maxDepth: Int,
  iterations: Int,
  shrinkage: Double,
  featureSampleRatio: Double,
  dataSampleRatio: Double,
  minLeafSize: Int,
  enableInitialGuess: Boolean,
  debug: Boolean,
  loss: LossType,
  maxBins: Int
)
