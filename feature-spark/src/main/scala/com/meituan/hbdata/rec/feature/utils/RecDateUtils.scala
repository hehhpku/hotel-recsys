package com.meituan.hbdata.rec.feature.utils

import java.text.SimpleDateFormat
import java.util.Calendar

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 12/18/15
 */
object RecDateUtils {
  val simpleDateFormat = new SimpleDateFormat("yyyyMMdd")

  //日期常量
  val yesterday = RecDateUtils.getNumDayBeforeString(1)
  val today = RecDateUtils.getNumDayBeforeString(0)
  val day7Before = RecDateUtils.getNumDayBeforeString(7)
  val day2Before = RecDateUtils.getNumDayBeforeString(2)
  val day30Before = RecDateUtils.getNumDayBeforeString(30)
  val day90Before = RecDateUtils.getNumDayBeforeString(90)

  def getNumDayBeforeString(dayToMinus: Int)={
    val cal = Calendar.getInstance
    cal.add(Calendar.DAY_OF_MONTH,0 - dayToMinus)
    simpleDateFormat.format(cal.getTime)
  }
  
  def getTimeStamp(dayToMinus: Int) = {
    val cal = Calendar.getInstance
    cal.add(Calendar.DAY_OF_MONTH,0 - dayToMinus)
    (cal.getTimeInMillis/1000 ).toString
  }

  def main(args: Array[String]) {
    println(getTimeStamp(1))
  }
}
