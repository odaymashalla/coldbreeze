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
import main.scala.simple_parts.process.Units._
import models.DAO.sessions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
import scala.util.Try
import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory
import us.ority.min.actions._
import us.ority.min.jobs._

object TestBuilder2 extends App {
  println("test")
  def await[T](a: Awaitable[T])(implicit ec: ExecutionContext) = Await.result(a, Duration.Inf)

  val processF = BuildF.run(97, invoke = true)
  val process = await(processF)
  println(process)
  //NInvoker.toApplogger("Launched " + process.get.session_id + " session")
  //Build.newRunFrom(bpID = 46, session_id = process.get.session_id, params = List(
  //  ReactionActivator(reaction_id = 1),
  //  ReactionActivator(reaction_id = 2)
  //))
}

object BuildF {
  // Resume hoook for launchStack
  def resumeProcess(bpID:Int,
                    session_id: Int, reaction_id: Int):Future[BProcess] = {
    println("resume process hook called")
    val activator = ReactionActivator(reaction_id, Some("delay") )
    newRunFrom(bpID, session_id, invoke = true, params = List(activator))
  } 
  // Add to launchStack
  def addToLaunchStack(process: BProcess) = {
    LaunchStackFactory.addE(LaunchStackElement(
      process,
      resumeProcess
    ))
  }

  protected lazy val appLogger: Logger = Logger(LoggerFactory.getLogger("build"))
  def toApplogger(msg: Any, log_type: String = "info")(implicit line: sourcecode.Line, file: sourcecode.File) = {
      log_type match {
        //case "debug" => appLogger.info(msg.toString)
        case _ => {
          //Future { println(s"${file.value}:${line.value} ${msg.toString}") }
          //Future { appLogger.info(msg.toString) }
          println(s"${file.value}:${line.value} ${msg.toString}")
          //appLogger.info(msg.toString)
        }
        //case "error" => appLogger.info(msg.toString)
      }
  }

  def testLogger = {
    for( a <- 1 to 30){
         println( "a: " + a );
    }
    for( a <- 1 to 30){
         appLogger.info( "a: " + a)
    }
    for( a <- 1 to 10){
        play.api.Logger.info( "a: " + a)
    }
  }

  /**
  * Initial launch
  * @bpID id of process
  * @lang optional language
  **/
  def run(bpID: Int, lang: Option[String] = Some("en"), invoke: Boolean,
  pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List.empty ):Future[BProcess] = {
    //presenceValidate
    val bpDTO = BPDAO.get(bpID).get
    val processRunned1F:Future[BProcess] = initiate(bpID, invoke, bpDTO, session_id = None, pipes = pipes)
    processRunned1F.map { processRunned1 =>
       processRunned1.allElements.foreach { element =>
            //println()
            toApplogger("Title " + element.title + " " + element.id)
            toApplogger("states: " + element.states.length + ". switchers: " +element.states.map(st => st.switchers.length).length)
            //println()
            toApplogger("session_states: " + element.session_states.length)
            element.session_states.foreach {
              state =>
              //println()
              toApplogger("Title :   " + state.title)
              toApplogger("Status:   " + state.on)
            }
            toApplogger("reactions: " + element.reactions.length)
            println("middlewares: " + element.reactions.head.middlewares.length)
            //println()
       }
        toApplogger("Process #" + processRunned1.id + " session states: ")
        processRunned1.session_states.foreach {
          state =>
          //println()
          toApplogger("Title :   " + state.title)
          toApplogger("Status:   " + state.on)
        }
        toApplogger("Title :   " + "Paused")
        toApplogger("Status:   " + processRunned1.station.paused)        
        toApplogger("elements length" + processRunned1.allElements.length)
        toApplogger("************************************************")
        toApplogger("************************************************")
        println("------------------------------------------------------")

        processRunned1
    }
  }
  /**
   * Run from launch
   * @bpID bpID of process
   * @session_id id of session
   * @params params for reaction
   **/
  def newRunFrom(bpID:Int,
                 session_id: Int,
                 params: List[ReactionActivator] = List(),
                 invoke:Boolean = true,
                 process_dto:Option[BProcessDTO]=None,
                 station_dto:Option[BPStationDTO]=None,
                 minimal: Boolean = false):Future[BProcess]  = {
    println("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<")
    println(s"NEW RUN FROM (pid: ${bpID}, session_id: ${session_id} ${params} invoke:${invoke})")
    println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>")
      val bpDTO = process_dto match {
        case Some(dto) => dto
        case _ => BPDAO.get(bpID).get
      }
      initiate(bpID, invoke, bpDTO, session_id = Some(session_id), params = params, minimal = minimal)
  }

  def newRunFromWithElements(bpID:Int,
                 session_id: Int,
                 params: List[ReactionActivator],
                 invoke:Boolean = true,
                 process_dto:Option[BProcessDTO]=None,
                 station_dto:Option[BPStationDTO]=None,
                 minimal: Boolean = false):Future[ProcessExecutionResult] =
   {
      val bpDTO = process_dto match {
        case Some(dto) => dto
        case _ => BPDAO.get(bpID).get
      }
      initiateWithElements(bpID, invoke, bpDTO, session_id = Some(session_id), params = params, minimal = minimal)
   }


  def saveSession(bprocess: BProcess, bprocess_dto: BProcessDTO, lang: Option[String] = Some("en")) = {
    val session = BPSession(
                            None,
                            bprocess_dto.id.get,
                            created_at = Some(org.joda.time.DateTime.now() ),
                            updated_at = Some(org.joda.time.DateTime.now() )
                        )
    BPSessionDAOF.pull(session)
  }

  def saveSessionStates(bprocess: BProcess,
                        bprocess_dto: BProcessDTO,
                        session_id: Int,
                        pulling: Boolean = false,
                        elemMap: scala.collection.mutable.Map[Int,Int]     = scala.collection.mutable.Map().empty,
                        spaceMap: scala.collection.mutable.Map[Int,Int]    = scala.collection.mutable.Map().empty,
                        spaceElsMap: scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty,
                        initialStateMap: scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty) = {
    val origin_states = BPStateDAOF.findByBP(bprocess_dto.id.get)
    play.api.Logger.debug("saveSessionStates")
        play.api.Logger.debug("elemMap")
    elemMap.foreach { el => play.api.Logger.debug(s"${el._1}, ${el._2}") }
        play.api.Logger.debug("spaceMap")
    spaceMap.foreach { el => play.api.Logger.debug(s"${el._1}, ${el._2}") }
        play.api.Logger.debug("spaceElsMap")
    spaceElsMap.foreach { el => play.api.Logger.debug(s"${el._1}, ${el._2}") }

      val session_states = origin_states.map(state =>
        BPSessionState(
          None,
          bprocess_dto.id.get,
          session_id,
          state.title,
          state.neutral,
          state.process_state,
          state.on,
          state.on_rate,
          elemMap.get(state.front_elem_id.getOrElse(0)),
          spaceElsMap.get(state.space_elem_id.getOrElse(0)),
          spaceMap.get(state.space_id.getOrElse(0)),
          origin_state = initialStateMap.get(state.id.getOrElse(0)),
          created_at = Some(org.joda.time.DateTime.now()),
          updated_at = Some(org.joda.time.DateTime.now()),
          lang = state.lang,
          middle = state.middle,
          middleable = state.middleable,
          oposite = state.oposite,
          opositable = state.opositable,
          session_elements = Some(SessionElements(elemMap.get(state.front_elem_id.getOrElse(0)),
                                                              spaceMap.get(state.space_id.getOrElse(0)),
                                                              spaceElsMap.get(state.space_elem_id.getOrElse(0))))))

    var ids:List[Int] = List()
    if (pulling) {
        var ids:List[Int] = session_states.map(session_state => BPSessionStateDAOF.pull_new_object(session_state)).filter(id => id != -1)
    }
    SessionStatesContainer(session_states, ids)
}
def saveOrUpdateSessionStates(bprocess: BProcess, bprocess_dto: BProcessDTO, session_id: Int, pulling: Boolean = false) = {
    val origin_states = BPStateDAOF.findByBP(bprocess_dto.id.get)
    val session_states_initial = SessionInitialStateDAO.findBySession(session_id).map(in => ExperimentalSessionBuilder.fromInitialState(in))
    //BPSessionStateDAO.findByOriginIds(origin_states.map(_.id.get))
    val session_states:List[BPSessionState] = (bprocess.allElements.map(_.session_states.toList)).flatten ++ bprocess.session_states.toList

    toApplogger("session states FOR UPDATE: ")
    session_states.foreach { state =>
        toApplogger(s"session state ${state.id} ${state.title} ${state.on} :: ${state.on_rate} ")
    }
    val deltas = session_states.filter { state =>
      session_states_initial.find(initialState => initialState.id == state.origin_state &&
      (initialState.on_rate != state.on_rate || initialState.on != state.on)).isDefined // TODO: 4-Dimension checking
    }
    var ids:List[Int] = List()
    if (pulling) {
      var ids:List[Int] = deltas.map(session_state => BPSessionStateDAOF.updateB(session_state.id.get, session_state))
    }

      SessionStatesContainer(session_states, ids)
  }

  def saveOrUpdateState(bprocess: BProcess, bprocess_dto: BProcessDTO, session_id: Int, lang: Option[String] = Some("en"), run_proc: Boolean = true) = {
    val station = BPStationDAO.from_origin_station(bprocess.station, bprocess_dto, session_id = session_id)
    val station_id = BPStationDAO.saveOrUpdate(station, lang, run_proc)
    station_id
  }
  def saveLogsInit(bprocess: BProcess, bprocess_dto: BProcessDTO, station_id: Int, spacesDTO: List[BPSpaceDTO]) = {
    val dblogger = BPLoggerDAOF.from_origin_lgr(bprocess.logger, bprocess_dto, station_id, spacesDTO)
    dblogger.foreach(log => BPLoggerDAOF.pull_object(log))
  }
  def saveSessionStateLogs(bprocess: BProcess, bprocess_dto: BProcessDTO) = {
    bprocess.station.station_logger.session_state_logs.foreach(obj => SessionStateLogDAO.pull_object(obj))
  }
  def saveStationLog(process_id: Int, station_id: Int, bprocess: BProcess) = {
    val station_loggers = bprocess.station.station_logger.logs.map(s => BPStationLoggeDAOF.from_origin_station(process_id, station_id, s))
    station_loggers.foreach(s => BPStationLoggeDAOF.pull_object(s))
  }
  def saveLaunchAct(bprocess: BProcess, bprocess_dto: BProcessDTO) = {
    bprocess.acts.foreach(act => 
      val actId = ActionActsDAOF.pull(act)
      act.results.foreach(ar =>
        ActionActResultsDAOF.pull(ar.copy(act = actId))
      )
    )
  }

  /** ***
    * Core launch
    * @param bpID
    * @return BProcess original instance
    */
def initiate(bpID: Int,
             run_proc: Boolean,
             bpDTO: BProcessDTO,
             lang: Option[String] = Some("en"),
             session_id: Option[Int],
             params: List[ReactionActivator] = List(),
             pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List(),
             minimal: Boolean = false ):Future[BProcess] = {

ElementRegistrator.apply
// caster
//val process_dto = BPDAO.get(bpID).get
val procElemsF = ProcElemDAOF.findByBPId(bpID)
val process = new BProcess(new Managment, id = bpDTO.id)

initiate2(bpID, run_proc, process, bpDTO, procElemsF, lang, with_pulling = true, session_id_val = session_id,
          params = params, pipes = pipes, minimal = minimal)
}
def initiateWithElements(bpID: Int,
             run_proc: Boolean,
             bpDTO: BProcessDTO,
             lang: Option[String] = Some("en"),
             session_id: Option[Int],
             params: List[ReactionActivator] = List(),
             pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List(),
             minimal: Boolean = false ):Future[ProcessExecutionResult] = {

ElementRegistrator.apply
// caster
//val process_dto = BPDAO.get(bpID).get
val procElemsF = ProcElemDAOF.findByBPId(bpID)
val process = new BProcess(new Managment, id = bpDTO.id)
initiateWithElements2F(bpID, run_proc, process, bpDTO, procElemsF, lang, with_pulling = true, session_id_val = session_id,
          params = params, pipes = pipes, minimal = minimal)
}


def initiateWithElements2F(bpID: Int,
              run_proc: Boolean,
              processRunned: BProcess,
              bpDTO: BProcessDTO,
              procElementsF: Future[Seq[UndefElement]],
              lang: Option[String] = Some("en"),
              with_pulling: Boolean = false,
              session_id_val: Option[Int],
              params: List[ReactionActivator] = List(),
              pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List(),
              minimal: Boolean = false ):Future[ProcessExecutionResult] =
{
    val test_spaceF = BPSpaceDAOF.findByBPId(bpID)
    val space_elemsF = SpaceElemDAOF.findByBPId(bpID)
    procElementsF.flatMap { procElementsObj =>
    test_spaceF.flatMap { test_spaceObj =>
    space_elemsF.flatMap { space_elemsObj =>

    val procElements = procElementsObj.toList
    val test_space = test_spaceObj.toList
    val space_elems = space_elemsObj.toList

    val process = processRunned


  //process_dto
  // val session_id = 1 // REMOVE THIS!!

    var session_id: Int = 0
    var runFrom: Boolean = false // RUNFROM BOOLEAN CONSTANT
    var sessionEls:List[SessionUndefElement] = List()
    var sessionSpaces: List[SessionSpaceDTO] = List()
    var sessionSpaceEls: List[SessionSpaceElementDTO] = List()

    // Maps Origin to Session
    var elemMap:scala.collection.mutable.Map[Int, Int]    = scala.collection.mutable.Map().empty
    var spaceMap:scala.collection.mutable.Map[Int, Int]   = scala.collection.mutable.Map().empty
    var spaceElsMap:scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty

    var burnElemMap:scala.collection.mutable.Map[Int,Int]    = scala.collection.mutable.Map().empty
    var burnSpaceMap:scala.collection.mutable.Map[Int,Int]   = scala.collection.mutable.Map().empty
    var burnSpaceElMap:scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty

    // Generate sessions
    session_id_val match {
      case Some(session_val) => { // Launch from existed session
        runFrom = true
        session_id = session_val
        //val sessionElsF:Future[Seq[SessionUndefElement]]         = SessionProcElementDAOF.findBySession(session_id)
        //val sessionSpacesF:Future[Seq[SessionSpaceDTO]]          = SessionSpaceDAOF.findBySession(session_id)
        //val sessionSpaceElsF:Future[Seq[SessionSpaceElementDTO]] = SessionSpaceElemDAOF.findBySession(session_id)
        //sessionElsF     .map { sessionElsObj =>
        //sessionSpacesF  .map { sessionSpacesObj =>
        //sessionSpaceElsF.map { sessionSpaceElsObj =>
          sessionEls      = SessionProcElementDAO.findBySession(session_id)//sessionElsObj.toList
          sessionSpaces   = SessionSpaceDAO.findBySession(session_id)//sessionSpacesObj.toList
          sessionSpaceEls = SessionSpaceElemDAO.findBySession(session_id)//sessionSpaceElsObj.toList
        //}
        //}
        //}
      }
      case _ => { // launch from empty session
        session_id = models.DAO.sessions.SessionProcElementDAOF.await(
          saveSession(processRunned, bpDTO, lang) )
        // FRONT ELEM NOT FOR BRICKS
        sessionEls = procElements.map { el =>
            val obj = ExperimentalSessionBuilder.fromOriginEl(el, session_id, burnElemMap)
            elemMap += el.id.get -> obj.id.get
            obj
        }.toList
        sessionSpaces = test_space.map { sp =>//.filter(sp => sp.brick_nested.isDefined).map ( sp =>
          val obj = ExperimentalSessionBuilder.fromOriginSp(sp, session_id, elemMap, spaceElsMap)
          spaceMap += sp.id.get -> obj.id.get
          obj
        }
        ExperimentalAfterBurning.makeBurn(spaceMap, burnElemMap)
        sessionSpaceEls = space_elems.map { spel =>//.filter(n => n.space_own.isDefined).map ( spel =>
          val obj = ExperimentalSessionBuilder.fromOriginSpElem(spel, session_id, spaceMap)
          spaceElsMap += spel.id.get -> obj.id.get
          obj
        }
      }
    }

    // Update session for process
    processRunned.session_id = session_id
    val station_id = saveOrUpdateState(processRunned, bpDTO, session_id, lang, run_proc)
    // session state linked with session elements
    //val session_states_ids = saveSessionStates(processRunned, bpDTO, session_id)


    saveLogsInit(processRunned, bpDTO, station_id, BPSpaceDAOF.findByBPIdB(bpID))
    saveStationLog(bpID, station_id, processRunned)


  /****** Physical element casting  */
  val target2 = sessionEls.map(el => ExperimentalSessionBuilder.fromSessionEl(el))
  val arrays = target2.map(c => c.cast(process)).flatten.toArray
  process.push {
    arrays.sortWith(_.order < _.order)
  }
  toApplogger("elements " + process.allElements.length + " " + procElements.length)

  /* Presence validation  */
  val front_bricks = process.findFrontBrick()
  val test_space2 = sessionSpaces.map(sp => ExperimentalSessionBuilder.fromSessionSp(sp))
  val space_elems2 = sessionSpaceEls.map(el => ExperimentalSessionBuilder.fromSessionSpEl(el))
  if (front_bricks.length > 0 && test_space2.length > 0) {
    makeFrontSpaces(process, test_space2, front_bricks, space_elems2)
    if (test_space2.reduceLeft(getLatestNest).nestingLevel > 1) {
      makeNestedSpaces(process, test_space2, process.findNestedBricks(), space_elems2)
    }
  }

/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
    var states:List[BPState] = List()
    if (!minimal) {
    states = BPStateDAOF.findByBP(bpID)
    }
    /**
     * Session state
     * Retriving
     * with_pulling: Boolean - use db for save session and retrive ids
     **/
    var session_states:   List[BPSessionState]              = List()
    var initialStates:    List[SessionInitialState]         = List()
    var sessionTopologs:  List[SessionElemTopology]         = List()
    var sessionSwitchers: List[SessionUnitSwitcher]         = List()
    var sessionReactions: List[SessionUnitReaction]         = List()
    var sessionReactOuts: List[SessionUnitReactionStateOut] = List()

    var sessionMiddlewares:List[LaunchMiddleware] = List()
    var sessionStrategies:List[LaunchStrategy] = List()
    var sessionStBases:List[LaunchStrategyBaseUnit] = List()
    var sessionStInputs:List[LaunchStrategyInputUnit] = List()
    var sessionStOuts:List[LaunchStrategyOutputUnit] = List()

    // Maps Origin to Session
    var initialStateMap:scala.collection.mutable.Map[Int, Int]    = scala.collection.mutable.Map().empty
    var TopologsMap:scala.collection.mutable.Map[Int, Int]        = scala.collection.mutable.Map().empty
    var SwitchersMap:scala.collection.mutable.Map[Int, Int]       = scala.collection.mutable.Map().empty
    var ReactionsMap:scala.collection.mutable.Map[Int,Int]        = scala.collection.mutable.Map().empty
    var ReactOutsMap:scala.collection.mutable.Map[Int,Int]        = scala.collection.mutable.Map().empty

    var MiddlewaresMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StrategiesMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StBasesMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StInputsMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StOutsMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty

    if (runFrom && !minimal) { // Run from session
        initialStates    = SessionInitialStateDAO.findBySession(session_id)
        states           = initialStates.map(in => ExperimentalSessionBuilder.fromInitialState(in))
        sessionTopologs  = SessionElemTopologDAO.findBySession(session_id)
        sessionSwitchers = SessionSwitcherDAO.findBySession(session_id)
        sessionReactions = SessionReactionDAO.findBySession(session_id)
        sessionReactOuts = SessionReactionStateOutDAO.findByReactions(sessionReactions.map(react => react.id.get))

        sessionMiddlewares = LaunchMiddlewaresDAOF.await(LaunchMiddlewaresDAOF.findByReactions(sessionReactions.map(react => react.id.get)) ).toList
        sessionStrategies = LaunchStrategiesDAOF.await(LaunchStrategiesDAOF.findByMiddlewares(
          sessionMiddlewares.map(l => l.id.get).toList
        )).toList
        sessionStBases  = LaunchStrategyBasesDAOF.await( LaunchStrategyBasesDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList
        sessionStInputs = LaunchStrategyInputsDAOF.await(LaunchStrategyInputsDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList
        sessionStOuts   = LaunchStrategyOutputsDAOF.await(LaunchStrategyOutputsDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList

        val existedSesStates = BPSessionStateDAOF.await( BPSessionStateDAOF.findByBPAndSession(bpID, session_id) ).toList
        session_states = SessionStatesContainer(existedSesStates, existedSesStates.map(o => o.id.getOrElse(0))).session_states
    } else if( !minimal ) { // Run from plain


        states = states.map { state =>
            val ogState = ExperimentalSessionBuilder.fromOriginState(state, session_id, elemMap,spaceMap,spaceElsMap)
            initialStateMap += state.id.get -> ogState.id.get
            initialStates = ogState :: initialStates
            ExperimentalSessionBuilder.fromInitialState(ogState)
        }
        if (with_pulling) {
        //} else {
          session_states = Build.saveSessionStates(processRunned, bpDTO, session_id, pulling = true, elemMap, spaceMap, spaceElsMap, initialStateMap).session_states
          val existedSesStates = BPSessionStateDAOF.await( BPSessionStateDAOF.findByBPAndSession(bpID, session_id) ).toList
          session_states = SessionStatesContainer(existedSesStates, existedSesStates.map(o => o.id.getOrElse(0))).session_states
        }

        //session_states = session_states.map(ss => ss.copy(origin_state = initialStateMap.get(ss.origin_state.getOrElse(0))))
        // Element TOPOLOGY
        sessionTopologs = ElemTopologDAO.findByBP(bpID).map { el =>
            val obj = ExperimentalSessionBuilder.fromOriginTopo(el, session_id, elemMap,spaceMap,spaceElsMap)
            TopologsMap += el.id.get -> obj.id.get
            obj
        }
        sessionSwitchers = SwitcherDAO.findByBPId(bpID).map { el =>
          val obj: SessionUnitSwitcher = ExperimentalSessionBuilder.fromOriginSwitcher(el, session_id, initialStateMap)
          SwitchersMap += el.id.get -> obj.id.get
          obj
        }

        sessionReactions = ReactionDAO.findByBP(bpID).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginReaction(el, session_id, TopologsMap, initialStateMap)
          ReactionsMap += el.id.get -> obj.id.get
          obj
        }
        sessionReactOuts = ReactionStateOutDAO.findByReactions(ReactionsMap.keys.toList).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginReactionStateOut(el, session_id, ReactionsMap, initialStateMap)
          ReactOutsMap += el.id.get -> obj.id.get
          obj
        }
        sessionMiddlewares = MiddlewaresDAOF.await(MiddlewaresDAOF.findByReactions(ReactionsMap.keys.toList) ).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginMiddlewares(el, session_id, ReactionsMap)
          MiddlewaresMap += el.id.get -> obj.id.get
          obj
        }.toList
        sessionStrategies = StrategiesDAOF.await(StrategiesDAOF.findByMiddlewares(MiddlewaresMap.keys.toList)).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginStrategies(el, session_id, MiddlewaresMap)
          StrategiesMap += el.id.get -> obj.id.get
          obj
        }.toList
        sessionStBases = StrategyBasesDAOF.await( StrategyBasesDAOF.getByStrategies(StrategiesMap.keys.toList) ).map  { el =>
          val obj = ExperimentalSessionBuilder.fromOriginStBases(el, StrategiesMap)
          StBasesMap += el.id.get -> obj.id.get
          obj
        }.toList
        sessionStInputs = StrategyInputsDAOF.await( StrategyInputsDAOF.getByStrategies(StrategiesMap.keys.toList) ).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginStInputs(el, StrategiesMap)
          StInputsMap += el.id.get -> obj.id.get
          obj
        }.toList
        sessionStOuts = StrategyOutputsDAOF.await( StrategyOutputsDAOF.getByStrategies(StrategiesMap.keys.toList) ).map { el =>
          val obj = ExperimentalSessionBuilder.fromOriginStOuts(el, StrategiesMap)
          StOutsMap += el.id.get -> obj.id.get
          obj
        }.toList



        toApplogger("[RED Initial Run Cast result: RESET]")
        toApplogger(s"states ${states.length}")
        toApplogger(s"sessionTopologs ${sessionTopologs.length}")
        toApplogger(s"sessionSwitchers ${sessionSwitchers.length}")
        toApplogger(s"sessionReactions ${sessionReactions.length}")
        toApplogger(s"sessionReactOuts ${sessionReactOuts.length}")
        toApplogger("REACT OUT FROM PLAIN RUN")
        ReactionsMap.values.toList.foreach { l => toApplogger(l)}
        toApplogger(s"${ReactionsMap.values.toList.length}")
    }




  val topologs                                      = sessionTopologs.map(el =>
              ExperimentalSessionBuilder.fromSessionTopo(el))
  val switches:List[UnitSwitcher]                   = sessionSwitchers.map(el =>
              ExperimentalSessionBuilder.fromSessionSwitcher(el))
  val reactions:List[UnitReaction]                  = sessionReactions.map(el =>
              ExperimentalSessionBuilder.fromSessionReaction(el))
  val reaction_state_out:List[UnitReactionStateOut] = sessionReactOuts.map(el =>
              ExperimentalSessionBuilder.fromSessionReactionStateOut(el))
// Actions
val middlewares:List[Middleware] = sessionMiddlewares.map(el => ExperimentalSessionBuilder.fromSessionMiddlewares(el))
val strategies:List[Strategy] = sessionStrategies.map(el => ExperimentalSessionBuilder.fromSessionStrategies(el))
val strategiy_bases:List[StrategyBaseUnit] = sessionStBases.map(el => ExperimentalSessionBuilder.fromSessionStBases(el))
val strategiy_inputs:List[StrategyInputUnit] = sessionStInputs.map(el => ExperimentalSessionBuilder.fromSessionStInputs(el))
val strategiy_outputs:List[StrategyOutputUnit] = sessionStOuts.map(el =>   ExperimentalSessionBuilder.fromSessionStOuts(el))


//val topologs = ElemTopologDAO.findByBP(bpID)
//val switches:List[UnitSwitcher] = SwitcherDAO.findByBPId(bpID)
//val reactions:List[UnitReaction] = ReactionDAO.findByBP(bpID)
//val reaction_state_out:List[UnitReactionStateOut] = ReactionStateOutDAO.findByReactions(reactions.map(react => react.id.get))
    process.topology = topologs

    toApplogger("states found: " + states.length)
    toApplogger("session_states found: " + session_states.length)
    states.foreach { state =>
      // toApplogger(state.front_elem_id.toString)
    }
    session_states.foreach { state =>
      // toApplogger(state.front_elem_id.toString)
    }
    reactions.foreach { react => react.reaction_state_outs ++= reaction_state_out.filter(sout => sout.reaction == react.id.get) }
    states.foreach { state => state.switchers ++= switches.filter(sw => sw.state_ref == state.id.get) }
    session_states.foreach { session_state => session_state.switchers ++= switches.filter(sw => Some(sw.state_ref) == session_state.origin_state)  }

    println("middlewares length are "+middlewares.length)
    // Fill pipes and middlewares
    reactions.foreach { reaction =>
      val reaction_middlewares = middlewares.filter(mw => mw.reaction == reaction.id.get)

      strategies.foreach { strategy =>
        //strategy.strategiy_bases
        //strategy.strategiy_inputs
        //strategy.strategiy_outputs
        strategiy_inputs.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyInputUnit(a) }
        strategiy_bases.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyBaseUnit(a) }
        strategiy_outputs.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyOutputUnit(a) }

        middlewares.foreach { middleware =>
          if (strategy.middleware == middleware.id.get) {
            middleware.pushToStrategies(strategy)
          }
        }
      }
      reaction_middlewares.foreach { reaction_middleware =>   reaction.middlewares += reaction_middleware }

    }

    process.states ++= states.filter(state => state.process_state == true)
    process.session_states ++= session_states.filter(state => state.process_state == true)


    def topoIdFetch(topo: Option[ElemTopology]):Option[Int] = {
      topo match {
       case Some(topo) => topo.id
       case _ => None
      }
    }


    process.variety.foreach { element =>
      element.states ++= states.filter(state => state.front_elem_id == Some(element.id))
      element.session_states ++=  session_states.filter(state => state.front_elem_id == Some(element.id))
      element.reactions ++= reactions.filter { react =>
        val pred_elem = topologs.find(topo => topo.front_elem_id == Some(element.id))
        Some(react.element) == topoIdFetch(pred_elem)
      }
    }
    process.spacesElements.foreach { element =>
      element.states ++=  states.filter(state => state.space_elem_id == Some(element.id))
      element.session_states ++=  session_states.filter(state => state.space_elem_id == Some(element.id))
      element.reactions ++= reactions.filter(react => Some(react.element) == topologs.find(topo => topo.space_elem_id == Some(element.id)).get.id )
    }
    process.spaces.foreach { space =>
      space.states ++=  states.filter(state => state.space_id == space.id)
      space.session_states ++=  session_states.filter(state => state.space_id == space.id)
    }

    addToLaunchStack(process)

    /**
    * Activate reaction by params
    **/
    params.foreach { param =>
      println(s"param detected ${param.toString}")
      val reaction = process.allElements.map(el => el.reactions).flatten
        reaction.find(reaction => reaction.id == Some(param.reaction_id)) match {
        case Some(target_reaction) => {
          if (param.target.isDefined && param.target == Some( "delay") ) {
            val el = bprocesses.ReactionExecutor.retriveElementByAction(process, target_reaction)
            el.get.session_states.find(s => s.title == "delayed") match {
              case Some(s) => { 
                println("finded delayed action")
                s.on = true
                s.on_rate = 100
              }
              case _ =>
            }
          } else { // TODO: Refactor this
            target_reaction.execute(process)
          }
        }
        case _ =>
      }
    }
    println("params are executed")

  /************************************************************************************************/
  /*************************** BEFORE LAUNCH ******************************************************/
    val launchPipe = LaunchMapPipe(
    launchId = session_id,
    elementPipe =     ElementPipe(
                                  elemMap,
                                  spaceMap,
                                  spaceElsMap),
    burnElementPipe =     BurnElementPipe(
                                   burnElemMap,
                                   burnSpaceMap,
                                   burnSpaceElMap),
    aditElementPipe =     AditElementPipe(
                                    initialStateMap,
                                    TopologsMap,
                                    SwitchersMap,
                                    ReactionsMap,
                                    ReactOutsMap)
    )
    val executedPipes:List[ExecutedLaunchCVPipes] = pipes.map(pipe => pipe(launchPipe))

    // TO STACK

/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
    println("start invoking process with NInvoker")
    if (validateElements(procElements.toList, test_space, space_elems) && run_proc)
      NInvoker.run_proc(process)
    else
      toApplogger("Process launch flag is off")
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/*************************** AFTER LAUNCH *******************************************************/
/************************************************************************************************/
/************************************************************************************************/
/**
    *  Save state and logs only if process was runned
    **/
    if (run_proc && !minimal) {
      saveLogsInit(process, bpDTO, station_id, test_space)
      saveOrUpdateSessionStates(process, bpDTO, session_id, pulling = true)
      saveSessionStateLogs(process, bpDTO)
      saveOrUpdateState(process, bpDTO, session_id, lang)
      saveStationLog(bpID, station_id, process)
      saveLaunchAct(process, bpDTO)
    }

    Future( ProcessExecutionResult( 
              process, 
              procElementsObj.toList,
              test_spaceObj.toList,
              space_elemsObj.toList ) )
    }
    }
  }
}



























































def initiate2(bpID: Int,
              run_proc: Boolean,
              processRunned: BProcess,
              bpDTO: BProcessDTO,
              procElementsF: Future[Seq[UndefElement]],
              lang: Option[String] = Some("en"),
              with_pulling: Boolean = false,
              session_id_val: Option[Int],
              params: List[ReactionActivator] = List(),
              pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List(),
              minimal:Boolean = false ):Future[BProcess] = {
 initiate2F(bpID,
                          run_proc,
                          processRunned,
                          bpDTO,
                          procElementsF,
                          lang,
                          with_pulling,
                          session_id_val,
                          params,
                          pipes, minimal = minimal)
}

def initiate2F(bpID: Int,
              run_proc: Boolean,
              processRunned: BProcess,
              bpDTO: BProcessDTO,
              procElementsF: Future[Seq[UndefElement]],
              lang: Option[String] = Some("en"),
              with_pulling: Boolean = false,
              session_id_val: Option[Int],
              params: List[ReactionActivator] = List(),
              pipes: List[LaunchMapPipe => ExecutedLaunchCVPipes]=List(),
              minimal: Boolean = false ):Future[BProcess] =
{
    val test_spaceF = BPSpaceDAOF.findByBPId(bpID)
    val space_elemsF = SpaceElemDAOF.findByBPId(bpID)
    procElementsF.flatMap { procElementsObj =>
    test_spaceF.flatMap { test_spaceObj =>
    space_elemsF.flatMap { space_elemsObj =>

    val procElements = procElementsObj.toList
    val test_space = test_spaceObj.toList
    val space_elems = space_elemsObj.toList

    val process = processRunned


  //process_dto
  // val session_id = 1 // REMOVE THIS!!

    var session_id: Int = 0
    var runFrom: Boolean = false // RUNFROM BOOLEAN CONSTANT
    var sessionEls:List[SessionUndefElement] = List()
    var sessionSpaces: List[SessionSpaceDTO] = List()
    var sessionSpaceEls: List[SessionSpaceElementDTO] = List()

    // Maps Origin to Session
    var elemMap:scala.collection.mutable.Map[Int, Int]    = scala.collection.mutable.Map().empty
    var spaceMap:scala.collection.mutable.Map[Int, Int]   = scala.collection.mutable.Map().empty
    var spaceElsMap:scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty

    var burnElemMap:scala.collection.mutable.Map[Int,Int]    = scala.collection.mutable.Map().empty
    var burnSpaceMap:scala.collection.mutable.Map[Int,Int]   = scala.collection.mutable.Map().empty
    var burnSpaceElMap:scala.collection.mutable.Map[Int,Int] = scala.collection.mutable.Map().empty

    // Generate sessions
    session_id_val match {
      case Some(session_val) => { // Launch from existed session
        runFrom = true
        session_id = session_val
        //val sessionElsF:Future[Seq[SessionUndefElement]]         = SessionProcElementDAOF.findBySession(session_id)
        //val sessionSpacesF:Future[Seq[SessionSpaceDTO]]          = SessionSpaceDAOF.findBySession(session_id)
        //val sessionSpaceElsF:Future[Seq[SessionSpaceElementDTO]] = SessionSpaceElemDAOF.findBySession(session_id)
        //sessionElsF     .map { sessionElsObj =>
        //sessionSpacesF  .map { sessionSpacesObj =>
        //sessionSpaceElsF.map { sessionSpaceElsObj =>
          sessionEls      = SessionProcElementDAO.findBySession(session_id)//sessionElsObj.toList
          sessionSpaces   = SessionSpaceDAO.findBySession(session_id)//sessionSpacesObj.toList
          sessionSpaceEls = SessionSpaceElemDAO.findBySession(session_id)//sessionSpaceElsObj.toList
        //}
        //}
        //}
      }
      case _ => { // launch from empty session
        session_id = models.DAO.sessions.SessionProcElementDAOF.await(
          saveSession(processRunned, bpDTO, lang) )
        // FRONT ELEM NOT FOR BRICKS
        sessionEls = procElements.map { el =>
            val obj = ExperimentalSessionBuilder.fromOriginEl(el, session_id, burnElemMap)
            elemMap += el.id.get -> obj.id.get
            obj
        }.toList
        sessionSpaces = test_space.map { sp =>//.filter(sp => sp.brick_nested.isDefined).map ( sp =>
          val obj = ExperimentalSessionBuilder.fromOriginSp(sp, session_id, elemMap, spaceElsMap)
          spaceMap += sp.id.get -> obj.id.get
          obj
        }
        ExperimentalAfterBurning.makeBurn(spaceMap, burnElemMap)
        sessionSpaceEls = space_elems.map { spel =>//.filter(n => n.space_own.isDefined).map ( spel =>
          val obj = ExperimentalSessionBuilder.fromOriginSpElem(spel, session_id, spaceMap)
          spaceElsMap += spel.id.get -> obj.id.get
          obj
        }
      }
    }

    // Update session for process
    processRunned.session_id = session_id
    val station_id = saveOrUpdateState(processRunned, bpDTO, session_id, lang, run_proc)
    // session state linked with session elements
    //val session_states_ids = saveSessionStates(processRunned, bpDTO, session_id)


    saveLogsInit(processRunned, bpDTO, station_id, BPSpaceDAOF.findByBPIdB(bpID))
    saveStationLog(bpID, station_id, processRunned)


  /****** Physical element casting  */
  val target2 = sessionEls.map(el => ExperimentalSessionBuilder.fromSessionEl(el))
  val arrays = target2.map(c => c.cast(process)).flatten.toArray
  process.push {
    arrays.sortWith(_.order < _.order)
  }
  toApplogger("elements " + process.allElements.length + " " + procElements.length)

  /* Presence validation  */
  val front_bricks = process.findFrontBrick()
  val test_space2 = sessionSpaces.map(sp => ExperimentalSessionBuilder.fromSessionSp(sp))
  val space_elems2 = sessionSpaceEls.map(el => ExperimentalSessionBuilder.fromSessionSpEl(el))
  if (front_bricks.length > 0 && test_space2.length > 0) {
    makeFrontSpaces(process, test_space2, front_bricks, space_elems2)
    if (test_space2.reduceLeft(getLatestNest).nestingLevel > 1) {
      makeNestedSpaces(process, test_space2, process.findNestedBricks(), space_elems2)
    }
  }

/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
/*************************************************************************************************************************/
    var states:List[BPState] = List()
    if (!minimal) {
    states = BPStateDAOF.findByBP(bpID)
    }
    /**
     * Session state
     * Retriving
     * with_pulling: Boolean - use db for save session and retrive ids
     **/
    var session_states:   List[BPSessionState]              = List()
    var initialStates:    List[SessionInitialState]         = List()
    var sessionTopologs:  List[SessionElemTopology]         = List()
    var sessionSwitchers: List[SessionUnitSwitcher]         = List()
    var sessionReactions: List[SessionUnitReaction]         = List()
    var sessionReactOuts: List[SessionUnitReactionStateOut] = List()

    // Maps Origin to Session
    var initialStateMap:scala.collection.mutable.Map[Int, Int]    = scala.collection.mutable.Map().empty
    var TopologsMap:scala.collection.mutable.Map[Int, Int]        = scala.collection.mutable.Map().empty
    var SwitchersMap:scala.collection.mutable.Map[Int, Int]       = scala.collection.mutable.Map().empty
    var ReactionsMap:scala.collection.mutable.Map[Int,Int]        = scala.collection.mutable.Map().empty
    var ReactOutsMap:scala.collection.mutable.Map[Int,Int]        = scala.collection.mutable.Map().empty

    var sessionMiddlewares:List[LaunchMiddleware] = List()
    var sessionStrategies:List[LaunchStrategy] = List()
    var sessionStBases:List[LaunchStrategyBaseUnit] = List()
    var sessionStInputs:List[LaunchStrategyInputUnit] = List()
    var sessionStOuts:List[LaunchStrategyOutputUnit] = List()

    var MiddlewaresMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StrategiesMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StBasesMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StInputsMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty
    var StOutsMap:scala.collection.mutable.Map[Long,Long]        = scala.collection.mutable.Map().empty


    if (runFrom && !minimal) { // Run from session
        initialStates    = SessionInitialStateDAO.findBySession(session_id)
        states           = initialStates.map(in => ExperimentalSessionBuilder.fromInitialState(in))
        sessionTopologs  = SessionElemTopologDAO.findBySession(session_id)
        sessionSwitchers = SessionSwitcherDAO.findBySession(session_id)
        sessionReactions = SessionReactionDAO.findBySession(session_id)
        sessionReactOuts = SessionReactionStateOutDAO.findByReactions(sessionReactions.map(react => react.id.get))

        sessionMiddlewares = LaunchMiddlewaresDAOF.await(LaunchMiddlewaresDAOF.findByReactions(sessionReactions.map(react => react.id.get)) ).toList
        sessionStrategies = LaunchStrategiesDAOF.await(LaunchStrategiesDAOF.findByMiddlewares(
          sessionMiddlewares.map(l => l.id.get).toList
        )).toList
        sessionStBases  = LaunchStrategyBasesDAOF.await( LaunchStrategyBasesDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList
        sessionStInputs = LaunchStrategyInputsDAOF.await(LaunchStrategyInputsDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList
        sessionStOuts   = LaunchStrategyOutputsDAOF.await(LaunchStrategyOutputsDAOF.getByStrategies(sessionStrategies.map(l =>
          l.id.get
        ).toList) ).toList


        val existedSesStates = BPSessionStateDAOF.await( BPSessionStateDAOF.findByBPAndSession(bpID, session_id) ).toList
        session_states = SessionStatesContainer(existedSesStates, existedSesStates.map(o => o.id.getOrElse(0))).session_states
    } else if( !minimal ) { // Run from plain
        states = states.map { state =>
            val ogState = ExperimentalSessionBuilder.fromOriginState(state, session_id, elemMap,spaceMap,spaceElsMap)
            initialStateMap += state.id.get -> ogState.id.get
            initialStates = ogState :: initialStates
            ExperimentalSessionBuilder.fromInitialState(ogState)
        }
        if (with_pulling) {
        //} else {
          session_states = Build.saveSessionStates(processRunned, bpDTO, session_id, pulling = true, elemMap, spaceMap, spaceElsMap, initialStateMap).session_states
          val existedSesStates = BPSessionStateDAOF.await( BPSessionStateDAOF.findByBPAndSession(bpID, session_id) ).toList
          session_states = SessionStatesContainer(existedSesStates, existedSesStates.map(o => o.id.getOrElse(0))).session_states
        }

        //session_states = session_states.map(ss => ss.copy(origin_state = initialStateMap.get(ss.origin_state.getOrElse(0))))
        // Element TOPOLOGY
        sessionTopologs = ElemTopologDAO.findByBP(bpID).map { el =>
            val obj = ExperimentalSessionBuilder.fromOriginTopo(el, session_id, elemMap,spaceMap,spaceElsMap)
            TopologsMap += el.id.get -> obj.id.get
            obj
        }
      sessionSwitchers = SwitcherDAO.findByBPId(bpID).map { el =>
        val obj: SessionUnitSwitcher = ExperimentalSessionBuilder.fromOriginSwitcher(el, session_id, initialStateMap)
        SwitchersMap += el.id.get -> obj.id.get
        obj
      }

      sessionReactions = ReactionDAO.findByBP(bpID).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginReaction(el, session_id, TopologsMap, initialStateMap)
        ReactionsMap += el.id.get -> obj.id.get
        obj
      }
      sessionReactOuts = ReactionStateOutDAO.findByReactions(ReactionsMap.keys.toList).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginReactionStateOut(el, session_id, ReactionsMap, initialStateMap)
        ReactOutsMap += el.id.get -> obj.id.get
        obj
      }

      sessionMiddlewares = MiddlewaresDAOF.await(MiddlewaresDAOF.findByReactions(ReactionsMap.keys.toList) ).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginMiddlewares(el, session_id, ReactionsMap)
        MiddlewaresMap += el.id.get -> obj.id.get
        obj
      }.toList
      sessionStrategies = StrategiesDAOF.await(StrategiesDAOF.findByMiddlewares(MiddlewaresMap.keys.toList)).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginStrategies(el, session_id, MiddlewaresMap)
        StrategiesMap += el.id.get -> obj.id.get
        obj
      }.toList
      sessionStBases = StrategyBasesDAOF.await( StrategyBasesDAOF.getByStrategies(StrategiesMap.keys.toList) ).map  { el =>
        val obj = ExperimentalSessionBuilder.fromOriginStBases(el, StrategiesMap)
        StBasesMap += el.id.get -> obj.id.get
        obj
      }.toList
      sessionStInputs = StrategyInputsDAOF.await( StrategyInputsDAOF.getByStrategies(StrategiesMap.keys.toList) ).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginStInputs(el, StrategiesMap)
        StInputsMap += el.id.get -> obj.id.get
        obj
      }.toList
      sessionStOuts = StrategyOutputsDAOF.await( StrategyOutputsDAOF.getByStrategies(StrategiesMap.keys.toList) ).map { el =>
        val obj = ExperimentalSessionBuilder.fromOriginStOuts(el, StrategiesMap)
        StOutsMap += el.id.get -> obj.id.get
        obj
      }.toList


      toApplogger("[RED Initial Run Cast result: RESET]")
      toApplogger(s"states ${states.length}")
      toApplogger(s"sessionTopologs ${sessionTopologs.length}")
      toApplogger(s"sessionSwitchers ${sessionSwitchers.length}")
      toApplogger(s"sessionReactions ${sessionReactions.length}")
      toApplogger(s"sessionReactOuts ${sessionReactOuts.length}")
      toApplogger("REACT OUT FROM PLAIN RUN")
      ReactionsMap.values.toList.foreach { l => toApplogger(l)}
      toApplogger(s"${ReactionsMap.values.toList.length}")
    }




  val topologs                                      = sessionTopologs.map(el =>
              ExperimentalSessionBuilder.fromSessionTopo(el))
  val switches:List[UnitSwitcher]                   = sessionSwitchers.map(el =>
              ExperimentalSessionBuilder.fromSessionSwitcher(el))
  val reactions:List[UnitReaction]                  = sessionReactions.map(el =>
              ExperimentalSessionBuilder.fromSessionReaction(el))
  val reaction_state_out:List[UnitReactionStateOut] = sessionReactOuts.map(el =>
              ExperimentalSessionBuilder.fromSessionReactionStateOut(el))

              // Actions
              val middlewares:List[Middleware] = sessionMiddlewares.map(el => ExperimentalSessionBuilder.fromSessionMiddlewares(el))
              val strategies:List[Strategy] = sessionStrategies.map(el => ExperimentalSessionBuilder.fromSessionStrategies(el))
              val strategiy_bases:List[StrategyBaseUnit] = sessionStBases.map(el => ExperimentalSessionBuilder.fromSessionStBases(el))
              val strategiy_inputs:List[StrategyInputUnit] = sessionStInputs.map(el => ExperimentalSessionBuilder.fromSessionStInputs(el))
              val strategiy_outputs:List[StrategyOutputUnit] = sessionStOuts.map(el =>   ExperimentalSessionBuilder.fromSessionStOuts(el))


//val topologs = ElemTopologDAO.findByBP(bpID)
//val switches:List[UnitSwitcher] = SwitcherDAO.findByBPId(bpID)
//val reactions:List[UnitReaction] = ReactionDAO.findByBP(bpID)
//val reaction_state_out:List[UnitReactionStateOut] = ReactionStateOutDAO.findByReactions(reactions.map(react => react.id.get))
    process.topology = topologs

    toApplogger("states found: " + states.length)
    toApplogger("session_states found: " + session_states.length)
    states.foreach { state =>
      // toApplogger(state.front_elem_id.toString)
    }
    session_states.foreach { state =>
      // toApplogger(state.front_elem_id.toString)
    }
    reactions.foreach { react => react.reaction_state_outs ++= reaction_state_out.filter(sout => sout.reaction == react.id.get) }
    states.foreach { state => state.switchers ++= switches.filter(sw => sw.state_ref == state.id.get) }
    session_states.foreach { session_state => session_state.switchers ++= switches.filter(sw => Some(sw.state_ref) == session_state.origin_state)  }

    process.states ++= states.filter(state => state.process_state == true)
    process.session_states ++= session_states.filter(state => state.process_state == true)

    println("middlewares length are "+middlewares.length)
    // Fill pipes and middlewares
    reactions.foreach { reaction =>
      val reaction_middlewares = middlewares.filter(mw => mw.reaction == reaction.id.get)

      strategies.foreach { strategy =>
        //strategy.strategiy_bases
        //strategy.strategiy_inputs
        //strategy.strategiy_outputs
        strategiy_inputs.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyInputUnit(a) }
        strategiy_bases.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyBaseUnit(a) }
        strategiy_outputs.filter(l => l.strategy == strategy.id.get).foreach { a => strategy.pushToStrategyOutputUnit(a) }


        middlewares.foreach { middleware =>
          if (strategy.middleware == middleware.id.get) {
            middleware.pushToStrategies(strategy)
          }
        }
      }
      reaction_middlewares.foreach { reaction_middleware =>   reaction.middlewares += reaction_middleware }

    }


    def topoIdFetch(topo: Option[ElemTopology]):Option[Int] = {
      topo match {
       case Some(topo) => topo.id
       case _ => None
      }
    }


    process.variety.foreach { element =>
      element.states ++= states.filter(state => state.front_elem_id == Some(element.id))
      element.session_states ++=  session_states.filter(state => state.front_elem_id == Some(element.id))
      element.reactions ++= reactions.filter { react =>
        val pred_elem = topologs.find(topo => topo.front_elem_id == Some(element.id))
        Some(react.element) == topoIdFetch(pred_elem)
      }
    }
    process.spacesElements.foreach { element =>
      element.states ++=  states.filter(state => state.space_elem_id == Some(element.id))
      element.session_states ++=  session_states.filter(state => state.space_elem_id == Some(element.id))
      element.reactions ++= reactions.filter(react => Some(react.element) == topologs.find(topo => topo.space_elem_id == Some(element.id)).get.id )
    }
    process.spaces.foreach { space =>
      space.states ++=  states.filter(state => state.space_id == space.id)
      space.session_states ++=  session_states.filter(state => state.space_id == space.id)
    }

    addToLaunchStack(process)

    /**
    * Activate reaction by params
    **/
    params.foreach { param =>
      println(s"param detected ${param.toString}")
      val reaction = process.allElements.map(el => el.reactions).flatten
        reaction.find(reaction => reaction.id == Some(param.reaction_id)) match {
        case Some(target_reaction) => {
          if (param.target.isDefined && param.target == Some( "delay") ) {
            val el = bprocesses.ReactionExecutor.retriveElementByAction(process, target_reaction)
            el.get.session_states.find(s => s.title == "delayed") match {
              case Some(s) => { 
                println("finded delayed action")
                s.on = true
                s.on_rate = 100
              }
              case _ =>
            }
          } else { // TODO: Refactor this
            target_reaction.execute(process)
          }
        }
        case _ =>
      }
    }
    println("params are executed")


  /************************************************************************************************/
  /*************************** BEFORE LAUNCH ******************************************************/
    val launchPipe = LaunchMapPipe(
    launchId = session_id,
    elementPipe =     ElementPipe(
                                  elemMap,
                                  spaceMap,
                                  spaceElsMap),
    burnElementPipe =     BurnElementPipe(
                                   burnElemMap,
                                   burnSpaceMap,
                                   burnSpaceElMap),
    aditElementPipe =     AditElementPipe(
                                    initialStateMap,
                                    TopologsMap,
                                    SwitchersMap,
                                    ReactionsMap,
                                    ReactOutsMap)
    )
    val executedPipes:List[ExecutedLaunchCVPipes] = pipes.map(pipe => pipe(launchPipe))
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
    if (validateElements(procElements.toList, test_space, space_elems) && run_proc)
      NInvoker.run_proc(process)
    else
      toApplogger("Process launch flag is off")
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/************************************************************************************************/
/*************************** AFTER LAUNCH *******************************************************/
/************************************************************************************************/
/************************************************************************************************/
/**
    *  Save state and logs only if process was runned
    **/
    if (run_proc && !minimal) {
      saveLogsInit(process, bpDTO, station_id, test_space)
      saveOrUpdateSessionStates(process, bpDTO, session_id, pulling = true)
      saveSessionStateLogs(process, bpDTO)
      saveOrUpdateState(process, bpDTO, session_id, lang)
      saveStationLog(bpID, station_id, process)
      saveLaunchAct(process, bpDTO)
    }

    Future( process )
    }
    }
  }
}




























  /*
  * Helpers
  */
  def makeFrontSpaces(process: BProcess, spacesDTO:List[BPSpaceDTO], bricks: Array[Brick], spaceElems: List[SpaceElementDTO]) = {
    val frontSpaces:List[BPSpaceDTO] = spacesDTO.filter(spaceDTO => spaceDTO.brick_front.isDefined )
    val frontSpacesElems = spaceElems.filter(spaceElem => frontSpaces.map(_.id.get).contains(spaceElem.space_owned))
    val origSpaces:List[Space] = frontSpaces.map(space => space.castFront(process, frontSpacesElems)).flatten.toList
    fillFrontSpaceElems(process, origSpaces, frontSpacesElems.sortWith(_.order < _.order))
  }
  def fillFrontSpaceElems(process: BProcess, origSpaces: List[Space], FrontSpaceElems: List[SpaceElementDTO]) = {
    origSpaces.foreach { orig =>
      FrontSpaceElems.filter(fs => fs.space_owned == orig.id.get).foreach { spaceElem =>
        orig.addToSpace(spaceElem.castToSpace(process, orig).get, spaceElem.space_role.getOrElse("container")) // Default space role is container
      }
      process.spaces = process.spaces :+ orig
    }
  }
  def makeNestedSpaces(process: BProcess, spacesDTO:List[BPSpaceDTO], bricks: Array[Brick], spaceElems: List[SpaceElementDTO], level: Int = 2) {
    val nestedSpaces:List[BPSpaceDTO] = spacesDTO.filter(spaceDTO => spaceDTO.brick_nested.isDefined && spaceDTO.nestingLevel == level) // NESTING LEVEL
    val nestedSpacesElems = spaceElems.filter(spaceElem => nestedSpaces.map(_.id.get).contains(spaceElem.space_owned))
    val origSpaces:List[Space] = nestedSpaces.map(space => space.cast_nested(process, nestedSpacesElems)).flatten.toList
    fillNestedSpacesElems(process, spacesDTO, spaceElems, origSpaces, nestedSpacesElems.sortWith(_.order < _.order), level)

  }
  def fillNestedSpacesElems(process: BProcess,
                            test_space: List[BPSpaceDTO],
                            space_elems: List[SpaceElementDTO],
                            origSpaces: List[Space],
                            nestedSpacesElems: List[SpaceElementDTO],
                            level: Int) =
  {
    origSpaces.foreach { orig =>
      nestedSpacesElems.filter(fs => fs.space_owned == orig.id.get).sortWith(_.order < _.order).foreach { spaceElem =>
        orig.addToSpace(spaceElem.castToSpace(process, orig).get, spaceElem.space_role.getOrElse("container")) // Default space role is Container
      }
      process.spaces = process.spaces :+ orig
      if (isNestedOpsLeft(process, test_space)) {
        orig.findBrickInCont()
        makeNestedSpaces(process, test_space, orig.findBrickInCont(), space_elems, level + 1)
      }
    }
  }
  /*
  Checkers
  */
  def isNestedOpsLeft(process: BProcess, test_space: List[BPSpaceDTO]): Boolean = {
    val latestNest = test_space.reduceLeft(getLatestNest).nestingLevel
    val nowNest = test_space.find(_.id == process.spaces.last.id).get.nestingLevel
    latestNest != nowNest
  }
  def maxNestedLevel(s1: SpaceElementDTO, s2: SpaceElementDTO) = if (s1.space_owned > s2.space_owned) s1 else s2
  def maxNestedNow(s1: Space, s2: Space) = if (s1.id.get > s2.id.get) s1 else s2
  def getLatestNest(s1: BPSpaceDTO, s2: BPSpaceDTO) = if (s1.nestingLevel > s2.nestingLevel) s1 else s2
  /**
   * Validation for elements that store in spaces
   */
  def validateElements(front_elems:List[UndefElement], test_space: List[BPSpaceDTO], space_elems: List[SpaceElementDTO]): Boolean = {
    val frontsidz = front_elems.map(elem => elem.id)
    val spacidz = space_elems.map(elem => elem.id)
    val z = test_space.filter(space => !(frontsidz.contains(space.brick_front) || spacidz.contains(space.brick_nested))).length < 1
    val d = space_elems.filter(elem => test_space.contains(Some(elem.space_owned))).length < 1
    d && z
  }



  def initFrom(station_id:Int,
              bpID:Int,
              params: List[InputParamProc],
              process_dto:Option[BProcessDTO]=None,
              station_dto:Option[BPStationDTO]=None
              ):BProcess  = {

    val processObj:Option[BProcessDTO] = process_dto match {
      case Some(process) => Some(process)
      case _ => BPDAO.get(bpID)
    }
    val station:Option[BPStationDTO]= station_dto match {
      case Some(station_dto) => Some(station_dto)
      case _ => BPStationDAO.findById(station_id)
    }

    val target = ProcElemDAO.findByBPId(bpID)
    val process = new BProcess(new Managment, id = Some(bpID))
    val arrays = target.map(c => c.cast(process)).flatten.toArray
    process.push {
      arrays.sortWith(_.order < _.order)
    }
    val logger_db = BPLoggerDAOF.findByStation(station_id)

    val test_space = BPSpaceDAOF.findByBPIdB(bpID)
    val space_elems = SpaceElemDAO.findByBPId(bpID)
    val front_bricks = process.findFrontBrick()



    /*
      Presence validation
     */
    if (front_bricks.length > 0 && test_space.length > 0) {
      makeFrontSpaces(process, test_space, front_bricks, space_elems)
      if (test_space.reduceLeft(getLatestNest).nestingLevel > 1) {
        makeNestedSpaces(process, test_space, process.findNestedBricks(), space_elems)
      }
    }
    validateElements(target, test_space, space_elems)











  val logger_results = logger_db.map(log =>
    BPLoggerResult(
      process.findObjectById(log.element, log.space_elem).get,
      composite=None,
      order = log.order,
      space = log.space,
      station = process.station,
      invoked = log.invoked,
      expanded = log.expanded,
      container = log.container,
      date = log.date))


  process.logger.logs = logger_results.toArray

  process.station.update_variables(
    station.get.state,
    station.get.step,
    station.get.space,
    station.get.container_step.toArray,
    station.get.expand_step.toArray,
    station.get.spaces_ids.toArray,
    station.get.started,
    station.get.finished,
    station.get.inspace,
    station.get.incontainer,
    station.get.inexpands,
    station.get.paused
  )
    /* State sync */
    //process.restoreCVOfElems

  process
  }


}
