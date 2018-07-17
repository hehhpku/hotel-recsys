package com.meituan.hbdata.rec.feature.modelTraining

import com.meituan.hbdata.rec.feature.label.RecLabels
import org.apache.spark.sql._
/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/26/15
 */
class RawSample(x: Row, val featureValueMap: Map[String,String], val actionLabel: RecLabels, val isAction: Boolean, val userId: Int, val poi_id: Int) extends Serializable{
  val uuid = x.getAs[String]("uuid")
  val serviceTime = x.getAs[String]("actiontime")
  val sessionId = x.getAs[String]("globalid")
  val strategy = x.getAs[String]("strategy")
  val clientType = x.getAs[String]("clienttype")
  val version = x.getAs[String]("appversion")
  val accommodationtype = x.getAs[String]("accommodationtype")
  val sortingMethod = x.getAs[String]("sortingmethod")
  val offset = if (x.isNullAt(x.fieldIndex("offset"))) 0 else x.getAs[Int]("offset")
  val cityId = x.getAs[Int]("cityid")

  override def toString = s"RawSample($uuid, $serviceTime, $sessionId, $strategy, $clientType, $version, $accommodationtype, $sortingMethod, $offset, $cityId, $featureValueMap, $actionLabel, $isAction)"
}
