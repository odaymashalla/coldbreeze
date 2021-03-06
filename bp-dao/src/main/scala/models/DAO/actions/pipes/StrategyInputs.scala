package models.DAO

import us.ority.min.actions._


import models.DAO.conversion.DatabaseCred
//import slick.driver.PostgresDriver.api._
import models.DAO._
//import slick.driver.PostgresDriver.api._
import models.DAO.conversion.DatabaseFuture._
import com.github.nscala_time.time.Imports._
import models.DAO.conversion.DatabaseCred.dbConfig.driver.api._
import com.github.tototoshi.slick.PostgresJodaSupport._
import main.scala.bprocesses.refs._
import main.scala.simple_parts.process._


/*
val id: Option[Long],
				ident: String,
				created_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now),
			    updated_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now)

 */
class StrategyInputs(tag: Tag) extends Table[StrategyInputUnit](tag, "strategy_inputs") {
  def id          = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def strategy    = column[Long]("strategy_id")
  def op          = column[String]("op")
  def title       = column[String]("title")
  def desc        = column[Option[String]]("desc")

  def ident       = column[String]("ident")
  def targetType  = column[String]("target_type")

  def created_at  = column[Option[org.joda.time.DateTime]]("created_at")
  def updated_at  = column[Option[org.joda.time.DateTime]]("updated_at")


  def strategyFK= foreignKey("strategy_inputs_unit_strategy_fk", strategy, models.DAO.StrategiesDAOF.strategies)(_.id, onDelete = ForeignKeyAction.Cascade)


  def * = (id.?,
           strategy,
           op,
           title,
           desc,
           ident,
           targetType,
           created_at, updated_at) <> (StrategyInputUnit.tupled, StrategyInputUnit.unapply)
}

/*
val id: Option[Long],
strategy: Long,
op: String,
title: String,
desc: Option[String],
ident: String,
targetType: String,
created_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now),
updated_at:Option[org.joda.time.DateTime] = Some(org.joda.time.DateTime.now))
*/

object StrategyInputsDAOF {
  import akka.actor.ActorSystem
  import slick.backend.{StaticDatabaseConfig, DatabaseConfig}
  import slick.jdbc.meta.MTable
  import scala.concurrent.ExecutionContext.Implicits.global
  import scala.concurrent.duration.Duration
  import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
  import scala.util.Try
  def await[T](a: Awaitable[T])(implicit ec: ExecutionContext) = Await.result(a, Duration.Inf)
  
  val strategy_inputs = TableQuery[StrategyInputs]

  private def filterQuery(id: Long): Query[StrategyInputs, StrategyInputUnit, Seq] =
    strategy_inputs.filter(_.id === id)

  val create: DBIO[Unit] = strategy_inputs.schema.create
  val drop: DBIO[Unit] = strategy_inputs.schema.drop

  private def filterStrategiesQuery(ids: List[Long]): Query[StrategyInputs, StrategyInputUnit, Seq] =
    strategy_inputs.filter(_.strategy inSetBind ids)
  def getByStrategies(ids: List[Long]) = db.run(filterStrategiesQuery(ids).result)

  def pull(s: StrategyInputUnit):Future[Long] = db.run(strategy_inputs returning strategy_inputs.map(_.id) += s)
  def get(id: Long) = db.run(filterQuery(id).result.headOption)


  def ddl_create = db.run(create)
  def ddl_drop = db.run(drop)

}
