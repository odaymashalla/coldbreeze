package service
//import com.typesafe.scalalogging.slf4j.Logger
import models.DAO._
import main.scala.utils._
import main.scala.bprocesses._
import main.scala.resources.scopes._
import com.github.nscala_time.time.Imports._
import main.scala.bprocesses._
import main.scala.simple_parts.process.{CompositeValues, Brick, ProcElems, ContainerBrick}
import main.scala.simple_parts.process.control._
import main.scala.simple_parts.process.data._
import main.scala.utils._
import main.scala.resources.scopes._
import builders._
import main.scala.bprocesses.links._
import main.scala.utils.Space
import main.scala.utils.{InputParamProc, ReactionActivator}
import models.DAO.conversion.Implicits.fetch_cv
import main.scala.bprocesses._
import main.scala.simple_parts.process._
import models.DAO.sessions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
import scala.util.Try
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import us.ority.min.actions._
import us.ority.min.jobs._



object BuilderRunnerEntries {
  // entry with init
  // entry with clean run
  def cleanRun(bpID: Int, 
               lang: Option[String] = Some("en"), 
               invoke: Boolean,
               pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List.empty):BuildingPhases =
    BuildF.run(bpID, Some(lang), invoke = true, pipesList)


  // entry with resume
  def runResumed():BuildingPhases = 
    BuildF.newRunFrom(session_id = session_id, bpID = bpID, params = pmsResult.get, invoke = true)
}
