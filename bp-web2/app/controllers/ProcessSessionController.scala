package controllers

import models.DAO.resources.{BusinessDAO, BusinessDTO}
import models.DAO._

import play.api._
import play.api.mvc._
import play.twirl.api.Html

//{Action, Controller}
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.cache._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats
import play.api.data.format.Formatter
import play.api.data.FormError
import play.api.Logger

import views._
import models.User
import service.DemoUser
import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User2
import play.api.i18n.MessagesApi
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models.DAO.BProcessDTO
import models.DAO.BPDAO
import models.DAO._
import models.DAO.resources._
import models.DAO.CompositeValues
import play.api.Play.current

import main.scala.bprocesses._
import main.scala.simple_parts.process.Units._
import models.DAO.reflect._
import models.DAO.conversion._
import ProcHistoryDAO._
import helpers._
import decorators._
import builders._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

case class StationNoteMsg(msg: String)
case class LogsContainer(session_loggers: List[BPLoggerDTO],
                         process_histories: List[ProcessHistoryDTO],
                         stations: List[BPStationDTO],
                         input_logs: List[InputLogger] = List.empty,
                         state_logs: List[SessionStateLog] = List.empty,
                         procId: Int = 0)


import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User2
import play.api.i18n.MessagesApi
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{ Action, RequestHeader }

class ProcessSessionController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[User2, CookieAuthenticator],
  socialProviderRegistry: SocialProviderRegistry)
  extends Silhouette[User2, CookieAuthenticator] {

implicit val InputLoggerReads = Json.reads[InputLogger]
implicit val InputLoggerWrites = Json.format[InputLogger]
implicit val SessionStateLogReads  = Json.reads[SessionStateLog]
implicit val SessionStateLogWrites  = Json.format[SessionStateLog]

  implicit val CompositeVReads = Json.reads[CompositeValues]
  implicit val CompositeVWrites = Json.format[CompositeValues]
  implicit val logReads = Json.reads[BPLoggerDTO]
  implicit val logWrites = Json.format[BPLoggerDTO]
  implicit val ProcessHistoryDTOreads = Json.reads[ProcessHistoryDTO]
  implicit val ProcessHistoryDTOformat = Json.format[ProcessHistoryDTO]
  implicit val stationReads = Json.reads[BPStationDTO]
  implicit val stationWrites = Json.format[BPStationDTO]
  implicit val StationNoteReads = Json.reads[StationNoteMsg]
  implicit val StationNoteWrites = Json.format[StationNoteMsg]
  implicit val LogsContainerreads = Json.reads[LogsContainer]
  implicit val LogsContainerformat = Json.format[LogsContainer]
  implicit val AroundAttrReads = Json.reads[AroundAttr]
  implicit val AroundAttrWrites = Json.format[AroundAttr]
  implicit val ElemAroundReads = Json.reads[ElemAround]
  implicit val ElemAroundWrites = Json.format[ElemAround]
  implicit val ListAroundReads = Json.reads[ListAround]
  implicit val ListAroundWrites = Json.format[ListAround]
  implicit val sessionReads = Json.reads[BPSession]
  implicit val sessionWrites = Json.format[BPSession]
  implicit val BProcessDTOReads = Json.reads[BProcessDTO]
  implicit val BProcessDTOWrites = Json.format[BProcessDTO]
  implicit val SessionPeoplesReads = Json.reads[SessionPeoples]
  implicit val SessionPeoplesFormat = Json.format[SessionPeoples]
  implicit val SessionStatusReads = Json.reads[SessionStatus]
  implicit val SessionStatusWrites = Json.format[SessionStatus]
  implicit val SessionContainerReads = Json.reads[SessionContainer]
  implicit val SessionContainerWrites = Json.format[SessionContainer]


  implicit val CachedRemovedResourceDTOReads = Json.reads[CachedRemovedResourceDTO]
  implicit val CachedRemovedResourceDTOWrites = Json.format[CachedRemovedResourceDTO]
  implicit val DeltasContainerReads = Json.reads[DeltasContainer]
  implicit val DeltasContainerWrites = Json.format[DeltasContainer]


// GET         /bprocess/:id/stations
def station_index(id: Int) = SecuredAction { implicit request =>
  if (security.BRes.procIsOwnedByBiz(request.identity.businessFirst, id)) {

     val result = models.DAO.BPStationDAO.findByBPId(id) //BPStationDAO.findByBPId(id)
     Ok(Json.toJson(result))

  } else { Forbidden(Json.obj("status" -> "Not found")) }
}
// GET         /bprocess/stations
def all_stations() = SecuredAction { implicit request =>
  Ok(Json.toJson(BPStationDAO.getAll))
}
// GET          /bprocess/:BPid/sessions
def process_all_session(pid: Int) = SecuredAction { implicit request =>
    if (security.BRes.procIsOwnedByBiz(request.identity.businessFirst, pid)) {

      val sess = BPSessionDAO.findByProcess(pid)
      sess match {
        case Some(sessionContainer) => {
            //val updatedStatuses:List[SessionStatus] = sessionContainer.sessions.map(status => InputLoggerDAO.launchPeopleFetcher(status))
            //val updatedCN = updatedStatuses.map(status => sessionContainer.updateStatus(status))

            Ok(Json.toJson(InputLoggerDAO.fetchPeople(sessionContainer))) //InputLoggerDAO.launchPeopleFetcher(cn))))
       }
        case _ => Ok(Json.toJson(Map("status" -> 404)))
      }
    } else { Forbidden(Json.obj("status" -> "Not found")) }
}

// GET         /sessions
def all_sessions() = SecuredAction.async { implicit request =>
	val email = request.identity.emailFilled
  val sess_cnsF = BPSessionDAOF.findByBusiness(request.identity.businessFirst)
  //val updated_cns:List[SessionContainer] = sess_cns.map { cn =>
  //val updatedStatuses:List[SessionStatus] = cn.sessions.map(status => InputLoggerDAO.launchPeopleFetcher(status))
  //val updatedCN = updatedStatuses.map(status => cn.updateStatus(status))
  sess_cnsF.flatMap { sess_cns =>
    InputLoggerDAOF.fetchPeopleBySessions(sess_cns).map { sess_cns_with_peoples =>
        Ok(Json.toJson( sess_cns_with_peoples ))
    }
  }
}


// GET all_cached_sessions

def all_cached_sessions(timestamp:String) = SecuredAction.async { implicit request =>
	val email = request.identity.emailFilled
  val business = request.identity.businessFirst

  val sess_cnsF = BPSessionDAOF.findByBusiness(business, Some(timestamp))
  sess_cnsF.flatMap { sess_cns =>
    InputLoggerDAOF.fetchPeopleBySessions(sess_cns).flatMap { sess_cns_with_peoples =>
      val jsonSessions = Json.toJson( sess_cns_with_peoples.filter(c => c.sessions.length > 0) )
      println(s"business first $business")
      val deltasF = CachedRemovedResourcesDAO
                          .findAllByScope(business.toString,"workbench",
                              "launches",
                              Some(timestamp)
                          )
      deltasF.map { deltas =>
        val deltasJson = Json.toJson( deltas )
          Ok(
            JsObject(Seq(
              "c" -> jsonSessions,
              "deltas" -> deltasJson
           ))
         )
      }
    }
  }
}





// GET         /sessions/filter
def filtered_sessions(session_ids: List[Int]) = SecuredAction.async { implicit request =>
  val email = request.identity.emailFilled
  val sess_cnsF = BPSessionDAOF.findByBusinessAndIds(request.identity.businessFirst, session_ids)
  //val updated_cns:List[SessionContainer] = sess_cns.map { cn =>
  //val updatedStatuses:List[SessionStatus] = cn.sessions.map(status => InputLoggerDAO.launchPeopleFetcher(status))
  //val updatedCN = updatedStatuses.map(status => cn.updateStatus(status))
  sess_cnsF.flatMap { sess_cns =>
    InputLoggerDAOF.fetchPeopleBySessions(sess_cns).map { sess_cns_with_peoples =>
        Ok(Json.toJson( sess_cns_with_peoples ))
    }
  }
}


// /bprocess/:id/station/:station_id
def show_station(id: Int, station_id: Int) = SecuredAction { implicit request =>
  if (security.BRes.stationSecured(station_id, request.identity.emailFilled, request.identity.businessFirst)) {
     Ok(Json.toJson(BPStationDAO.findById(station_id)))
  } else { Forbidden(Json.obj("status" -> "Not found")) }
}
// /bprocess/:id/station/:station_id/halt
def halt_session(id: Int, session_id: Int) = SecuredAction { implicit request =>
  if (security.BRes.sessionSecured(session_id, request.identity.emailFilled, request.identity.businessFirst)) {

    val station_id = BPStationDAO.findBySession(session_id)
    station_id match {
      case Some(station) => BPStationDAO.haltUpdate(station.id.get,
        request.identity.businessFirst.toString);Ok(Json.toJson(Map("success" -> "halted")))
      case _ => Ok(Json.toJson(Map("failure" -> "not halted")))
    }
  } else { Forbidden(Json.obj("status" -> "Not found")) }
}



// POST        /session/:id/unlisted
def makeUnlisted(id: Int) = SecuredAction { implicit request =>
    if (security.BRes.sessionSecured(id, request.identity.emailFilled, request.identity.businessFirst)) {
         Ok(Json.toJson(BPSessionDAO.makeUnlisted(id)))
    } else { Forbidden(Json.obj("status" -> "Not found")) }

}
// POST        /session/:id/listed
def makeListed(id: Int) = SecuredAction { implicit request =>
  if (security.BRes.sessionSecured(id, request.identity.emailFilled, request.identity.businessFirst)) {
     Ok(Json.toJson(BPSessionDAO.makeListed(id)))
  } else { Forbidden(Json.obj("status" -> "Not found")) }
}
// DELETE /session/:session_id/
def delete_session(session_id: Int) = SecuredAction.async { implicit request =>
    if (security.BRes.sessionSecured(session_id, request.identity.emailFilled, request.identity.businessFirst)) {
      BPSessionDAO.delete(session_id, request.identity.businessFirst.toString ).map { result =>
        Ok(Json.toJson(result))
      }
    } else { Future.successful( Forbidden(Json.obj("status" -> "Not found")) ) }
}



// /bprocess/:id/station/:station_id/around
def stations_elems_around(id: Int, station_id: Int) = SecuredAction { implicit request =>
  if (security.BRes.procIsOwnedByBiz(request.identity.businessFirst, id)) {
      Ok(Json.toJson(AroundProcessElementsBuilder.detect(id, station_id)))
  } else { Forbidden(Json.obj("status" -> "Not found")) }
}




/**
 * Fetch all sessions logs for process
 */
 //GET         /bprocess/:id/logs
def logs_index(id: Int) = SecuredAction.async { implicit request =>
  if (security.BRes.procIsOwnedByBiz(request.identity.businessFirst, id)) {
      val processHistoriesF:Future[Seq[models.DAO.ProcessHistoryDTO]] = ProcHistoryDAO.getByProcessF(id)
      val session_loggers = BPLoggerDAO.findByBPId(id)
      val stations = BPStationDAO.findByBPId(id)
      val session_ids = stations.map(st => st.session)

      val input_logs = InputLoggerDAO.getBySessions(session_ids)
      val session_log = SessionStateLogDAO.getAllBySessions(session_ids)

    for {
      maybeHistory <- processHistoriesF
      result <- Future(Ok(Json.toJson(LogsContainer(session_loggers,
        maybeHistory.toList,
        stations,input_logs,
        session_log,
        id
        ))))
    } yield result

      //Ok(Json.toJson(LogsContainer(session_loggers, processHistories, stations,input_logs,session_log)))
  } else { Future(Forbidden(Json.obj("status" -> "Not found"))) }
}



//GET         /bprocess/allLogs/
def logs_indexes(ids: List[Int]) = SecuredAction.async { implicit request =>
  val secured_ids = ids.filter( id =>
    security.BRes.procIsOwnedByBiz(request.identity.businessFirst, id))

    val logsContainerF = Future.sequence(

      secured_ids.map { id =>
        val processHistoriesF:Future[Seq[models.DAO.ProcessHistoryDTO]] = ProcHistoryDAO.getByProcessF(id)
        val session_loggers = BPLoggerDAO.findByBPId(id)
        val stations = BPStationDAO.findByBPId(id)
        val session_ids = stations.map(st => st.session)

        val input_logs = InputLoggerDAO.getBySessions(session_ids)
        val session_log = SessionStateLogDAO.getAllBySessions(session_ids)
        processHistoriesF.map { maybeHistory =>
          LogsContainer(session_loggers, maybeHistory.toList, stations,input_logs,session_log, id)
        }
    } )
    for {
      logsContainer <- logsContainerF
    } yield Ok(Json.toJson(logsContainer))
      //Ok(Json.toJson(LogsContainer(session_loggers, processHistories, stations,input_logs,session_log)))
}





/**
 * Update station note
 */
// POST        /bprocess/:id/station/:station_id/note
def update_note(id: Int, station_id: Int) = SecuredAction(BodyParsers.parse.json) { implicit request =>
  val perm = true // TODO: Make permission !!!
  if (security.BRes.procIsOwnedByBiz(request.identity.businessFirst, id)) {

  request.body.validate[StationNoteMsg].map{
    case entity => {
        if (perm) {
          BPStationDAO.updateNote(station_id, entity.msg)
          Ok(Json.toJson(Map("success" -> s"station $id note updated")))
        } else {
          BadRequest("Access Denied")
        }

        }
    }.recoverTotal{
      e => BadRequest("formWithErrors")
    }
  } else { Forbidden(Json.obj("status" -> "Not found")) }
}


}