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

/*
  initiated: all elements loaded, launch initiated, process ready to run
  runned: process starts execution
  finished: process was completed own execution [doesn't mean process is completed, it's just complete execution!!!]
 */

case class BuildingPhases(initiated: Future[PreparedProcess], runned: Future[BProcess], finished: Future[BProcess])

/********
 * Prepared process object that ready to run
 */
case class PreparedProcess(
                        process:BProcess, 
                        bpDTO:BProcessDTO, 
                        station_id:Int, 
                        session_id:Int,
                        procElements:List[UndefElement], 
                        test_space:List[BPSpaceDTO], 
                        space_elems:List[SpaceElementDTO], 
                        run_proc:Boolean, 
                        minimal:Boolean, 
                        lang: Option[String]
 )