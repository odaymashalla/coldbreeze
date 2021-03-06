
package models.DAO.sessions

import slick.model.ForeignKeyAction
import slick.driver.PostgresDriver.api._
import com.github.nscala_time.time.Imports._
import com.github.tototoshi.slick.PostgresJodaSupport._

import models.DAO.BPDAO._
import models.DAO.resources.BusinessDTO._
import models.DAO.conversion.{DatabaseCred, Implicits}
import main.scala.simple_parts.process._

import main.scala.simple_parts.process.data.{Confirm, Constant}

class SessionProcElements(tag: Tag) extends Table[SessionUndefElement](tag, "session_proc_elements") {

  def id        = column[Int]("id", O.PrimaryKey, O.AutoInc) // This is the primary key column
  def title     = column[String]("title")
  def desc      = column[String]("desc")
  def business  = column[Int]("business_id")
  def bprocess  = column[Int]("bprocess_id")
  def session   = column[Int]("session_id")
  def b_type    = column[String]("b_type")
  def type_title= column[String]("type_title")

  def space_own = column[Option[Int]]("space_id")

  def order     = column[Int]("order")
  //def comps = column[Option[List[CompositeValues]]]("comps", O.DBType("compositevalues[]"))

  def created_at= column[Option[org.joda.time.DateTime]]("created_at")
  def updated_at= column[Option[org.joda.time.DateTime]]("updated_at")

  def * = (id.?, title, desc, business, bprocess, session, b_type, type_title, space_own, order,
           created_at, updated_at) <> (SessionUndefElement.tupled, SessionUndefElement.unapply)

  def businessFK = foreignKey("s_proc_el_business_fk", business, models.DAO.resources.BusinessDAO.businesses)(_.id, onDelete = ForeignKeyAction.Cascade)
  def bpFK       = foreignKey("s_proc_el_bprocess_fk", bprocess, models.DAO.BPDAOF.bprocesses)(_.id, onDelete = ForeignKeyAction.Cascade)
  def sessionFK  = foreignKey("s_proc_el_session_fk", session,  models.DAO.BPSessionDAOF.bpsessions)(_.id, onDelete = ForeignKeyAction.Cascade)

}

import main.scala.simple_parts.process._
import main.scala.bprocesses._

/*
  Case class
 */
case class ElemSessionCount(session_id: Int, count: Int)

case class SessionUndefElement(id: Option[Int],
                        title:String,
                        desc:String,
                        business:Int,
                        bprocess:Int,
                        session: Int,
                        b_type:String,
                        type_title:String,
                        space_own:Option[Int],
                        order:Int,
                        created_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now),
                        updated_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now)) {
  /*

  def cast(process: BProcess):Option[ProcElems] = {
// TODO: to space casting
// TODO: Refactor
    this match {
      case y if (y.b_type == "brick" && y.type_title == "container_brick") => {
        Option(
            new ContainerBrick(id.get, title, desc,None, process, b_type, type_title, order, space_parent_id = space_own)
          )
      }
      case constant if (constant.b_type == "block" && constant.type_title == "constant") => {
        Option(
           new Constant[Boolean](id.get, true, process, order, space_id = None, values = None)
        )
      }
      case x if (x.b_type == "block" && x.type_title == "test block") => {
        Option(
          new Block(id.get,title,desc,None,process,b_type,type_title,order)
        )
      }
      case conf if (conf.b_type == "block" && conf.type_title == "confirm") => {
        Option(
          new Confirm(id.get,title,desc,None,process,b_type,type_title,order))
      }
      case _ => Option(
          new Block(id.get,title,desc,None,process,b_type,type_title,order)
        )
    }

  }
*/
}
object SessionProcElementDAOF {
import akka.actor.ActorSystem
import slick.backend.{StaticDatabaseConfig, DatabaseConfig}
//import slick.driver.JdbcProfile
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.tototoshi.slick.PostgresJodaSupport._
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
import scala.util.Try
import models.DAO.conversion.DatabaseFuture._

  //import dbConfig.driver.api._ //
  def await[T](a: Awaitable[T])(implicit ec: ExecutionContext) = Await.result(a, Duration.Inf)
  
  val session_proc_elements = TableQuery[SessionProcElements]
  //private def filterQueryByProcess(process: Int): Query[ProcessHistoriesF, ProcessHistoryDTO, Seq] =
  //  bpsessions.filter(_.process === process)
  private def filterQuery(id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.id === id)
  private def filterBySessionQuery(id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.session === id)
  private def filterBySessionsQuery(ids: List[Int]): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.session inSetBind ids)

  private def filterByLaunchesQuery(ids: List[Int]): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.session inSetBind ids)

  def findByLaunchesIds(bpsId: List[Int]) = db.run(filterByLaunchesQuery(bpsId).result)


  def findBySessionLength(session_id: Int):Future[Int] = {
    db.run(filterBySessionQuery(session_id).length.result)
  }


def findBySessionsLength(session_ids: List[Int]):Future[Seq[ElemSessionCount]] = {
    val ids = session_ids.mkString(",")
    db.run(
      sql"""SELECT session_id, count(session_id) FROM session_proc_elements
          WHERE session_proc_elements.session_id
          IN (#${ids}) GROUP BY session_id""".as[(Int,Int)]
    ).map { tupleSeq =>
      tupleSeq.map { tuple => ElemSessionCount(tuple._1, tuple._2) }.toSeq
    }
}

def findBySession(session_id: Int):Future[Seq[SessionUndefElement]] = {
     db.run(filterBySessionQuery(session_id).result)
}

def findById(id: Int):Future[Option[SessionUndefElement]] = {
        db.run(filterQuery(id).result.headOption)
}



}

object SessionProcElementDAO {
  import akka.actor.ActorSystem
  import slick.backend.{StaticDatabaseConfig, DatabaseConfig}
  //import slick.driver.JdbcProfile
  import slick.driver.PostgresDriver.api._
  import slick.jdbc.meta.MTable
  import scala.concurrent.ExecutionContext.Implicits.global
  import com.github.tototoshi.slick.PostgresJodaSupport._
  import scala.concurrent.duration.Duration
  import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
  import scala.util.Try
  import models.DAO.conversion.DatabaseFuture._

  //import dbConfig.driver.api._ //
  def await[T](a: Awaitable[T])(implicit ec: ExecutionContext) = Await.result(a, Duration.Inf)
  
  val session_proc_elements = TableQuery[SessionProcElements]

  //private def filterQueryByProcess(process: Int): Query[ProcessHistoriesF, ProcessHistoryDTO, Seq] =
  //  bpsessions.filter(_.process === process)
  private def filterQuery(id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.id === id)
  private def filterBySessionQuery(id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.session === id)
  private def filterByBPQuery(id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.bprocess === id)
  private def filterByBPSessionQuery(id: Int, session_id: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(e => (e.bprocess === id) && (e.session === session_id ))

  private def filterBySessionsQuery(ids: List[Int]): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(_.session inSetBind ids)
  private def filterByLaunchesQuery(ids: List[Int]): Query[SessionProcElements, SessionUndefElement, Seq] =
      session_proc_elements.filter(_.session inSetBind ids)
  private def findByBPanOrderQuery(id: Int, order: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(el => el.bprocess === id && el.order === order)

  private def renewOrderQuery(bprocess: Int, order_num: Int): Query[SessionProcElements, SessionUndefElement, Seq] =
    session_proc_elements.filter(el => el.bprocess === bprocess && el.order > order_num)

  def findByBPId(id: Int) = {
     await(db.run(filterByBPQuery(id).result)).toList
  }

  def findByBPSessionId(id: Int, session_id: Int) = {
    await(db.run(filterByBPSessionQuery(id,session_id).result)).toList
  }

  def findBySession(session_id: Int) = {
    await(db.run(filterBySessionQuery(session_id).result)).toList
  }

  def getAll = {
    await(db.run(session_proc_elements.result)).toList
  }

  def lastOrderOfBP(id: Int):Int = {
      val xs = findByBPId(id).map(_.order)
      if (xs.isEmpty) 1
      else xs.max + 1
  }
  def findLengthByBPId(id: Int):Int = {
      val q3 = findByBPId(id)
      q3.length
  }

  def findById(id: Int):Option[SessionUndefElement] = {
     await(db.run(filterQuery(id).result.headOption))
  }

  def findByBPanOrder(id: Int, order: Int) = {
     await(db.run(findByBPanOrderQuery(id, order).result.headOption))
  }

  def pull_object(s: SessionUndefElement) =   {
      await( db.run(session_proc_elements returning session_proc_elements.map(_.id) += s))
  }

  def update(id: Int, entity: SessionUndefElement):Boolean = {
      findById(id) match {
      case Some(e) => {
        await( db.run(session_proc_elements.filter(_.id === id).update(entity) ))
        true
      }
      case None => false
      }
  }

  def updateSpaceOwn(id: Int, space_own:Int):Boolean = {
      findById(id) match {
      case Some(e) => {
        await( db.run(session_proc_elements.filter(_.id === id).update(e.copy(space_own = Some(space_own))) ))
        true
      }
      case None => false
      }
  }


  def delete(id: Int) = {
    val elem = findById(id)
    val res = await( db.run(session_proc_elements.filter(_.id === id).delete))
    elem match {
       case Some(el) => renewOrder(el.bprocess, el.order)
       case _ =>
    }
    res
  }

  def moveUp(bprocess: Int, element_id: Int) = {
      val minimum = findByBPId(bprocess).sortBy(_.order)
      findById(element_id) match {
        case Some(e) => {
          if (e.order > 1 && e.order != minimum.head.order) {
            await( db.run(
              session_proc_elements.filter(_.id === element_id).update(e.copy(order = e.order - 1))
            ))
            val ch = findById(minimum.find(_.order == (e.order - 1)).get.id.get).get
            await( db.run(
              session_proc_elements.filter(_.id === minimum.find(_.order == (e.order - 1)).get.id.get).update(ch.copy(order = ch.order + 1))
            ))
          }
          true
        }
        case None => false
      }
  }
  def moveDown(bprocess: Int, element_id: Int) = {
      val maximum = findByBPId(bprocess).sortBy(_.order)
      findById(element_id) match {
        case Some(e) => {
          if (e.order < maximum.last.order && e.order != maximum.last.order) {
            await( db.run(session_proc_elements.filter(_.id === element_id).update(e.copy(order = e.order + 1)) ))
            val ch = findById(maximum.find(_.order == (e.order + 1)).get.id.get).get
            await( db.run(session_proc_elements.filter(_.id === maximum.find(_.order == (e.order + 1)).get.id.get).update(ch.copy(order = ch.order - 1)) ))
          }
          true
        }
        case None => false
      }
  }

/*
(1,Some(16))
(3,Some(17))
(4,Some(18))
(6,Some(19))
.renewOrder(bprocess, 5)
(1,Some(16))
(3,Some(17))
(4,Some(18))
(5,Some(19))
*/
  def renewOrder(bprocess: Int, order_num: Int) = {
      val q3 = await(db.run(renewOrderQuery(bprocess, order_num).result)).toList
      val ordered = q3.zipWithIndex.map(el => el._1.copy(order = (el._2 + 1) + (order_num - 1)))
      ordered.foreach { el =>
         update(el.id.get, el)
      }
/*

    session_proc_elements.filter(_.bprocess === bprocess && _.order > order_num)
     .map(x => x.order)
     .update(_ + 1)

    */
  }


  val create: DBIO[Unit] = session_proc_elements.schema.create
  val drop: DBIO[Unit] = session_proc_elements.schema.drop
  def ddl_create = db.run(create)
  def ddl_drop = db.run(drop)
  /**
   * Delete a specific entity by id. If successfully completed return true, else false
   */
  //def delete(id: Int):Boolean =
  //    {
  //  findById(id) match {
  //    case Some(entity) => { session_proc_elements.filter(_.id === id).delete; true }
  //    case None => false
  //  }
  //  }

  /**
   * Update a specific entity by id. If successfully completed return true, else false
   */
  //def update(id: Int, entity: (Option[Int], String, String, Int, Int, String, String, Int)):Boolean = {
  //    {
  //    findById(id) match {
  //    case Some(e) => { session_proc_elements.filter(_.id === id).update(entity); true }
  //    case None => false
  //    }
  //  }
  //}
}
