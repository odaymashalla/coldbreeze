package models.DAO

import models.DAO.driver.MyPostgresDriver1.simple._
import com.github.tminglei.slickpg.composite._
import models.DAO.conversion.{DatabaseCred, Implicits}
import scala.slick.model.ForeignKeyAction
import org.joda.time.DateTime

class ProcessHistories(tag: Tag) extends Table[ProcessHistoryDTO](tag, "process_histories") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def master_acc = column[String]("master_acc")
  def bprocess = column[Int]("process_id")
  def date = column[DateTime]("date")
  def action = column[String]("action")
  def what = column[Option[String]]("what")

  def bpFK = foreignKey("bprocess_fk", bprocess, models.DAO.BPDAO.bprocesses)(_.id, onDelete = ForeignKeyAction.Cascade)
  def accFK = foreignKey("macc_fk", master_acc, models.AccountsDAO.accounts)(_.userId, onDelete = ForeignKeyAction.Cascade)


  def * = (id.?, master_acc, bprocess, action, date, what) <> (ProcessHistoryDTO.tupled, ProcessHistoryDTO.unapply)

}
/*
  Process commits
 */
class ProcessCommits(tag: Tag) extends Table[ProcessCommitDTO](tag, "process_commits") {
  def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  def title = column[String]("title")
  def bprocess = column[Int]("process_id")
  def created_at = column[DateTime]("created_at")

  def bpFK = foreignKey("bprocess_fk", bprocess, models.DAO.BPDAO.bprocesses)(_.id, onDelete = ForeignKeyAction.Cascade)


  def * = (id.?, title, bprocess, created_at) <> (ProcessCommitDTO.tupled, ProcessCommitDTO.unapply)

}

case class ProcessCommitDTO(var id: Option[Int], title: String = "", bprocess: Int, created_at: DateTime)

object ProcCommitDAO {

  import scala.util.Try
  import DatabaseCred.database
  val proc_commits = TableQuery[ProcessCommits]
  def ddl_create = {
    database withSession {
      implicit session =>
        proc_commits.ddl.create
    }
  }
  def ddl_drop = {
    database withSession {
      implicit session =>
        proc_commits.ddl.drop
    }
  }
}




/*
  Process Histories
 */
case class ProcessHistoryDTO(var id: Option[Int], master_acc: String, bprocess: Int, action: String, date: DateTime, what: Option[String] = None)


object ProcHistoryDAO {
  import scala.util.Try
  import DatabaseCred.database

  case object ProcCreated
  case object ProcUpdated
  case object ProdDeleted


  val proc_histories = TableQuery[ProcessHistories]
 
  def make_history[A](userId: String, bprocess: Int, action: A) = {
  	pull_object( ProcessHistoryDTO(None, userId, bprocess, action.getClass.getName.split("\\$").last.toLowerCase, DateTime.now() ) )
  }


  def pull_object(s: ProcessHistoryDTO) = database withSession {
    implicit session ⇒
      proc_histories returning proc_histories.map(_.id) += s
  }
  def get(k: Int) = database withSession {
    implicit session ⇒
      val q3 = for { s ← proc_histories if s.id === k } yield s 
      println(q3.selectStatement)
      println(q3.list)
      q3.list.headOption
  }
    /**
   * Update a business service
   * @param id
   * @param business service
   */
  def update(id: Int, history: ProcessHistoryDTO) = database withSession { implicit session ⇒
    val historyToUpdate: ProcessHistoryDTO = history.copy(Option(id))
    proc_histories.filter(_.id === id).update(historyToUpdate)
  } /**
   * Delete a business service
   * @param id
   */
  def delete(id: Int) = database withSession { implicit session ⇒

    proc_histories.filter(_.id === id).delete
  }
  /**
   * Count all business_services
   */
  def count: Int = database withSession { implicit session ⇒
    Query(proc_histories.length).first
  }



  def getAll = database withSession {
    implicit session ⇒
      val q3 = for { s ← proc_histories } yield s 
      q3.list.sortBy(_.id)

  }

  def ddl_create = {
    database withSession {
      implicit session =>
      proc_histories.ddl.create
    }
  }
  def ddl_drop = {
    database withSession {
      implicit session =>
        proc_histories.ddl.drop
    }
  }
}
