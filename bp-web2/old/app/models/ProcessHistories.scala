package models.DAO

//import slick.driver.PostgresDriver.api._
//import com.github.tminglei.slickpg.composite._
import models.DAO.conversion.{DatabaseCred, Implicits}
import org.joda.time.DateTime
//import slick.driver.PostgresDriver.api._
import scala.concurrent.Future
import slick.driver.PostgresDriver.api._
import com.github.nscala_time.time.Imports._
import com.github.tototoshi.slick.PostgresJodaSupport._
import com.github.tminglei.slickpg.composite._
//import com.github.tototoshi.slick.PostgresJodaSupport._
import scala.concurrent._
import ExecutionContext.Implicits.global
//  import slick.model.ForeignKeyAction
class ProcessHistories(tag: Tag) extends Table[ProcessHistoryDTO](tag, "process_histories") {
  def id       = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def acc      = column[String]("master_acc")
  def process  = column[Option[Int]]("process_id")
  def action   = column[String]("action")
  def what     = column[Option[String]]("what")
  def what_id  = column[Option[Int]]("what_id")
  def date     = column[org.joda.time.DateTime]("date")

  def bpFK     = foreignKey("pr_hist_bprocess_fk", process, models.DAO.BPDAOF.bprocesses)(_.id)
  //def accFK    = foreignKey("pr_hist_macc_fk", acc, models.AccountsDAO.accounts)(_.userId, onDelete = ForeignKeyAction.Cascade)


  def * = (id.?, acc, action, process, what, what_id, date) <> (ProcessHistoryDTO.tupled, ProcessHistoryDTO.unapply)

}




/*
  Process Histories
 */
case class ProcessHistoryDTO(var id: Option[Int],
  acc: String,
  action: String,
  process: Option[Int],
  what: Option[String] = None,
  what_id: Option[Int] = None,
  date: DateTime) {

}
object ProcHisCom {
  def apply(id: Option[Int],
    acc: String,
    action: String,
    process: Option[Int],
    what: Option[String] = None,
    what_id: Option[Int] = None,
    date: DateTime = org.joda.time.DateTime.now()):ProcessHistoryDTO = {
    new ProcessHistoryDTO(id, acc, action, process, what, what_id, date)
  }
  def processCreated = "process_created"
  def processUpdated = "process_updated"
  def processDeleted = "process_deleted"
  def processLaunched = "process_launched"
  def processFinished = "process_finished"

  def processResumed = "process_resumed"
  def elementCreated = "elem_created"
  def elementRenamed = "element_renamed"
  def elementDeleted = "element_deleted"
  def elementMovedUp = "element_up"
  def elementDown    = "element_down"
  def spaceElementCreated      = "space_elem_created"
  def spaceElementRenamed      = "space_element_renamed"
  def spaceElementDeleted      = "space_element_deleted"
  def spaceElementMovedUp      = "space_element_up"
  def spaceElementMovedDown    = "element_down"
  def permCreated              = "perm_created"
  def permDeleted              = "perm_deleted"
}

object ProcHistoryDAO {
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
  def awaitAndPrint[T](a: Awaitable[T])(implicit ec: ExecutionContext) = println(await(a))


  case object ProcCreated
  case object ProcUpdated
  case object ProdDeleted


  val proc_histories = TableQuery[ProcessHistories]
 /*
  def make_history[A](userId: String, bprocess: Int, action: A) = {
  	pull_object( ProcessHistoryDTO(None, userId, bprocess, action.getClass.getName.split("\\$").last.toLowerCase, DateTime.now() ) )
  }



  */

  def pull_object(s: ProcessHistoryDTO) =   {
      Future {
        controllers.UserActor.updateNotifiy(s.action, s.acc)
      }
      await(db.run(  proc_histories returning proc_histories.map(_.id) += s ))
  }
  def getByProcess(proc_id: Int):List[models.DAO.ProcessHistoryDTO] =   {

      val q3 = for { s ← proc_histories if s.process === proc_id } yield s
      await(db.run(q3.result)).toList//.headOption
  }
  def getByProcessF(proc_id: Int):Future[Seq[models.DAO.ProcessHistoryDTO]] = ProcHistoryDAOF.getByProcessF(proc_id)


  def update(id: Int, history: ProcessHistoryDTO) =   {
    val historyToUpdate: ProcessHistoryDTO = history.copy(Option(id))
    await(db.run(  proc_histories.filter(_.id === id).update(historyToUpdate) ))
  }
  def delete(id: Int) =   {
    await(db.run(  proc_histories.filter(_.id === id).delete ))
  }

  val create: DBIO[Unit] = proc_histories.schema.create
  val drop: DBIO[Unit] = proc_histories.schema.drop

  def ddl_create = db.run(create)
  def ddl_drop = db.run(drop)

}
/*
  Process commits

class ProcessCommits(tag: Tag) extends Table[ProcessCommitDTO](tag, "process_commits") {
  import slick.model.ForeignKeyAction

  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def bprocess = column[Int]("process_id")
  def created_at = column[DateTime]("created_at")

  def bpFK = foreignKey("bprocess_fk", bprocess, models.DAO.BPDAOF.bprocesses)(_.id, onDelete = ForeignKeyAction.Cascade)


  def * = (id.?, title, bprocess, created_at) <> (ProcessCommitDTO.tupled, ProcessCommitDTO.unapply)

}

case class ProcessCommitDTO(var id: Option[Int], title: String = "", bprocess: Int, created_at: DateTime)

object ProcCommitDAO {

  import scala.util.Try
  import DatabaseCred.database
  val proc_commits = TableQuery[ProcessCommits]
  def ddl_create = {
      {

        proc_commits.schema.create
    }
  }
  def ddl_drop = {
      {

        proc_commits.schema.drop
    }
  }
}

*/
