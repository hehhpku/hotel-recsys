package com.meituan.hbdata.rec.feature.label

/**
 *
 * Author: hehuihui@meituan.com
 * Date: 1/7/16
 */
class RecLabels(val isView: Int, val isOrder: Int, val isPay: Int) extends Serializable{

  override def toString = s"Labels($isView, $isOrder, $isPay)"
}
