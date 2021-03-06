package main.scala.simple_parts.process

import main.scala.bprocesses._
import main.scala.utils._
import com.github.nscala_time.time.Imports._
import scala.collection.mutable.ListBuffer
import us.ority.min.actions._
/******************************************************************
 * Session Units
 * Used only in CONVERSION BETWEN PLAIN
 * DO NOT USE IN REGULAR INVOKATION!!!!!! ONLY FOR CONVERSION!!!
 *****************************************************************/


case class SessionUnitSwitcher(id: Option[Int],
    bprocess: Int,
    session: Int,
    switch_type:String,
    priority: Int = 2,
    state_ref:Int,
    session_state_ref: Option[Int] = None,
    fn: String,
    target: String,
    override_group: Int = 0,
    created_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now),
    updated_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now)) {
}
