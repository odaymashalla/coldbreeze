package controllers
import utils.auth.DefaultEnv

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

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User2
import play.api.i18n.{ I18nSupport, MessagesApi }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models.DAO.BProcessDTO
import models.DAO.BPDAO
import models.DAO._
import models.DAO.resources._
import models.DAO.CompositeValues
import play.api.Play.current
import main.scala.bprocesses.BPSession

import models.DAO.reflect._
import main.scala.bprocesses.refs._
import main.scala.bprocesses.refs.{BPStateRef}
import models.DAO.conversion._


import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User2
import play.api.i18n.{ I18nSupport, MessagesApi }
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.mvc.{ Action, RequestHeader }
// case classes
/*
Ref
UnitElementRef
UnitSpaceRef
UnitSpaceElementRef
BPStateRef
UnitSwitcherRef
*/




class ReflectionsController @Inject() (
                                        val messagesApi: MessagesApi,
                                        silhouette: Silhouette[DefaultEnv],
                                        socialProviderRegistry: SocialProviderRegistry)
                                        extends Controller with I18nSupport {

implicit val RefReads = Json.reads[Ref]
implicit val RefWrites = Json.format[Ref]
implicit val UnitElementRefReads = Json.reads[UnitElementRef]
implicit val UnitElementRefWrites = Json.format[UnitElementRef]
implicit val UnitSpaceRefReads = Json.reads[UnitSpaceRef]
implicit val UnitSpaceRefWrites = Json.format[UnitSpaceRef]
implicit val UnitSpaceElementRefReads = Json.reads[UnitSpaceElementRef]
implicit val UnitSpaceElementRefWrites = Json.format[UnitSpaceElementRef]
implicit val BPStateRefReads = Json.reads[BPStateRef]
implicit val BPStateRefWrites = Json.format[BPStateRef]
implicit val UnitSwitcherRefReads = Json.reads[UnitSwitcherRef]
implicit val UnitSwitcherRefWrites = Json.format[UnitSwitcherRef]
implicit val RefElemTopologyReads = Json.reads[RefElemTopology]
implicit val RefElemTopologyWrites = Json.format[RefElemTopology]

implicit val UnitReactionRefReads = Json.reads[UnitReactionRef]
implicit val UnitReactionRefWrites = Json.format[UnitReactionRef]
implicit val UnitReactionStateOutRefReads = Json.reads[UnitReactionStateOutRef]
implicit val UnitReactionStateOutRefWrites = Json.format[UnitReactionStateOutRef]

implicit val MiddlewareRefReads = Json.reads[MiddlewareRef]
implicit val StrategyRefReads = Json.reads[StrategyRef]
implicit val StrategyInputRefReads = Json.reads[StrategyInputRef]
implicit val StrategyBaseRefReads = Json.reads[StrategyBaseRef]
implicit val StrategyOutputRefReads = Json.reads[StrategyOutputRef]

implicit val MiddlewareRefWrites = Json.format[MiddlewareRef]
implicit val StrategyRefWrites = Json.format[StrategyRef]
implicit val StrategyInputRefWrites = Json.format[StrategyInputRef]
implicit val StrategyBaseRefWrites = Json.format[StrategyBaseRef]
implicit val StrategyOutputRefWrites = Json.format[StrategyOutputRef]



implicit val ReactionContainerReads = Json.reads[ReactionContainer]
implicit val ReactionContainerWrites = Json.format[ReactionContainer]

implicit val RefContainerReads = Json.reads[RefContainer]
implicit val RefContainerWrites = Json.format[RefContainer]

def index() = silhouette.SecuredAction { implicit request =>
	val refs = RefDAO.getAllVisible
  val refs_collected = refs.map { ref =>
      val reactions = ReactionRefDAO.findByRef(ref.id.get)
      val reaction_outs = ReactionStateOutRefDAO.findByReactionRefs(reactions.map(_.id.get))
      val middlewares = MiddlewareRefsDAOF.await( MiddlewareRefsDAOF.getByRef(ref.id.get) )
      val middlewaresIds = middlewares.map(_.id.get).toList
      val strategies = MiddlewareRefsDAOF.await( StrategyRefsDAOF.getByMWS(middlewaresIds) )
      val strategiesIds = strategies.map(_.id.get).toList
      val bases = StrategyBaseRefsDAOF.await( StrategyBaseRefsDAOF.getByStrategies(strategiesIds) )
      val inputs = StrategyInputRefsDAOF.await( StrategyInputRefsDAOF.getByStrategies(strategiesIds) )
      val ouputs = StrategyInputRefsDAOF.await( StrategyOutputRefsDAOF.getByStrategies(strategiesIds) )


      RefContainer(ref,
        ProcElemReflectionDAO.findByRef(ref.id.get),
        SpaceReflectionDAO.findByRef(ref.id.get),
        SpaceElementReflectionDAO.findByRef(ref.id.get),
        ReflectElemTopologDAO.findByRef(ref.id.get),
        BPStateRefDAO.findByRef(ref.id.get),
        SwitcherRefDAO.findByRef(ref.id.get),
        reactions,
        reaction_outs,
        reactions.map( re => ReactionContainer(re, reaction_outs.filter(out => out.reaction == re.id.get ))),

        middlewares = middlewares,
        strategies = strategies,
        inputs = inputs,
        bases = bases,
        outputs = ouputs
)

  }

  Ok(Json.toJson(refs_collected))
}

/***
 * Create ref
 */
def create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[Ref].map{
    case entity => RefDAO.pull_object(entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity.title}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}


def elem_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>


  request.body.validate[UnitElementRef].map{
    case entity => ProcElemReflectionDAO.pull_object(entity.copy(order = ProcElemReflectionDAO.lastOrderOfRef(entity.reflection))) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create front element ${entity.title}")))
            case id =>  {
              makeTopolog(entity.reflection, Some(id), None)

              AutoTracer.defaultStatesForRefElem(entity.reflection, front_elem_id = Some(id), space_elem_id = None)
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}



def space_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>

  val placeResult = request.body.validate[UnitSpaceRef]
   request.body.validate[UnitSpaceRef].map{
    case entity => SpaceReflectionDAO.pull_object(entity.copy(index = SpaceReflectionDAO.lastIndexOfSpace(entity.reflection))) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create space ${entity.index}")))
            case id =>  {

                AutoTracer.defaultStatesForSpace(ref_id = entity.reflection, space_id = Some(id))
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}
def spaceelems_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>

  val placeResult = request.body.validate[UnitSpaceElementRef]
  request.body.validate[UnitSpaceElementRef].map{
    case entity => println(entity)
  }
  request.body.validate[UnitSpaceElementRef].map{
    case entity => SpaceElementReflectionDAO.pull_object(entity.copy(
                                                        order = SpaceElementReflectionDAO.lastOrderOfSpace(entity.reflection, entity.ref_space_owned))) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create space element ${entity.title}")))
            case id =>  {
              makeTopolog(entity.reflection, None, Some(id))
              AutoTracer.defaultStatesForRefElem(entity.reflection, front_elem_id = None, space_elem_id = Some(id))


              Ok(Json.toJson(Map("success" ->  id)))

            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}



def state_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[BPStateRef].map{
    case entity => BPStateRefDAO.pull_object(entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity.title}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}
def switche_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[UnitSwitcherRef].map{
    case entity => SwitcherRefDAO.pull_object(entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity.switch_type}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}



/***
 * Update
 */
def update(id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[Ref].map{
    case entity => RefDAO.update(id,entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not update ref ${entity.title}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}
def elem_update(elem_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[UnitElementRef].map{
    case entity => ProcElemReflectionDAO.update(elem_id,entity) match {
            case false =>  Ok(Json.toJson(Map("failure" ->  s"Could not update front element ${entity.title}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}

def space_update(space_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[UnitSpaceRef].map{
    case entity => SpaceReflectionDAO.update(space_id,entity) match {
            case false =>  Ok(Json.toJson(Map("failure" ->  s"Could not update space ${entity.id}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}

def spaceelems_update(spelem_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[UnitSpaceElementRef].map{
    case entity => SpaceElementReflectionDAO.update(spelem_id,entity) match {
            case false =>  Ok(Json.toJson(Map("failure" ->  s"Could not update space element ${entity.title}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }

}

def moveUpFrontElem(bpId: Int, elem_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  ProcElemReflectionDAO.moveUp(bpId, elem_id)
  Ok(Json.toJson("moved"))
}
def moveDownFrontElem(bpId: Int, elem_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  ProcElemReflectionDAO.moveDown(bpId, elem_id)
  Ok(Json.toJson("moved"))
}
def moveUpSpaceElem(id: Int, spelem_id: Int, space_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
    SpaceElementReflectionDAO.moveUp(id, spelem_id, space_id)
  Ok(Json.toJson("moved"))
}
def moveDownSpaceElem(id: Int, spelem_id: Int, space_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
    SpaceElementReflectionDAO.moveDown(id, spelem_id, space_id)
  Ok(Json.toJson("moved"))
}



// Misc
def state_update(state_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[BPStateRef].map{
    case entity => BPStateRefDAO.update(state_id,entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not update space element ${entity.title}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }

}


def switche_update(switch_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
  request.body.validate[UnitSwitcherRef].map{
    case entity => SwitcherRefDAO.update(switch_id,entity) match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not update  ${entity.switch_type}")))
            case _ =>  Ok(Json.toJson(entity.id))
          }
    }.recoverTotal{
      e => BadRequest(s"formWithErrors ${e.toString}")
    }
}







/***
 * Delete
 */
def delete(id: Int) = silhouette.SecuredAction { implicit request =>
    RefDAO.delete(id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}
def elem_delete(elem_id: Int) = silhouette.SecuredAction { implicit request =>
    ProcElemReflectionDAO.delete(elem_id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}

def space_delete(space_id: Int) = silhouette.SecuredAction { implicit request =>
    SpaceReflectionDAO.delete(space_id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}

def spaceelems_delete(spelem_id: Int) = silhouette.SecuredAction { implicit request =>
    SpaceElementReflectionDAO.delete(spelem_id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}

def state_delete(state_id: Int) = silhouette.SecuredAction { implicit request =>
    BPStateRefDAO.delete(state_id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}


def switche_delete(switch_id: Int) = silhouette.SecuredAction { implicit request =>
    SwitcherRefDAO.delete(switch_id) match {
        case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
        case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
      }
}






/********************************************************************************************************************
********************************************************************************************************************
                                                Action reflections
********************************************************************************************************************
*********************************************************************************************************************/

 def reaction_create() = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
   request.body.validate[UnitReactionRef].map{
     case entity => ReactionRefDAO.pull_object(entity) match {
             case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
             case id =>  {
               //entity.reaction_state_outs.foreach(out => ReactionStateOutRefDAO.pull_object(out))
               Ok(Json.toJson(Map("success" ->  id)))
             }
           }
     }.recoverTotal{
       e => BadRequest(s"formWithErrors ${e.toString}")
     }
 }
 def reaction_update(reaction_id: Int) = silhouette.SecuredAction(BodyParsers.parse.json) { implicit request =>
   request.body.validate[ReactionContainer].map{
     case entity => ReactionRefDAO.update(reaction_id,entity.reaction) match { // ReactionRefDAO ReactionStateOutRefDAO

             case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not update reaction ${entity.reaction.element}")))
             case _ =>  { val out_ids = ReactionStateOutRefDAO.findByReactionRef(reaction_id).map(_.id.get)
                          out_ids.foreach { id =>
                             entity.reaction_state_outs.find(_.id == Some(id)) match {
                               case Some(state_out) => ReactionStateOutRefDAO.update(id, state_out)
                               case _ => BadRequest(Json.toJson(Map("error" -> "cat unpdate state out")))
                             }
                           }
                           Ok(Json.toJson(entity.reaction.id))

                         }
           }
     }.recoverTotal{
       e => BadRequest(s"formWithErrors ${e.toString}")
     }

 }
 def reaction_delete(reaction_id: Int) = silhouette.SecuredAction { implicit request =>
     ReactionRefDAO.delete(reaction_id) match {
         case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
         case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
       }
 }

/*********************************************************************************************
// Action internals
**********************************************************************************************/
def create_middleware() = silhouette.SecuredAction.async(BodyParsers.parse.json) { implicit request =>
  request.body.validate[MiddlewareRef].map{
    case entity => MiddlewareRefsDAOF.pull(entity).map { obj =>
      obj match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
      }
    }.recoverTotal{
      e => Future.successful( BadRequest(s"formWithErrors ${e.toString}") )
    }
}
def create_strategy() = silhouette.SecuredAction.async(BodyParsers.parse.json) { implicit request =>
  request.body.validate[StrategyRef].map{
    case entity => StrategyRefsDAOF.pull(entity).map { obj =>
        obj match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
      }
    }.recoverTotal{
      e => Future.successful( BadRequest(s"formWithErrors ${e.toString}") )
    }
}


def delete_middleware(middleware_id: Long) = silhouette.SecuredAction.async { implicit request =>
  MiddlewareRefsDAOF.delete(middleware_id).map { res =>
    res match {
      case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
      case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
    }
  }
}
def delete_strategy(strategy_id: Long) = silhouette.SecuredAction.async { implicit request =>
  StrategyRefsDAOF.delete(strategy_id).map { res =>
    res match {
      case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
      case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
    }
  }
}


// Action pipes
def create_base() = silhouette.SecuredAction.async(BodyParsers.parse.json) { implicit request =>
  request.body.validate[StrategyBaseRef].map{
    case entity => StrategyBaseRefsDAOF.pull(entity).map { obj =>
       obj match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
        }
    }.recoverTotal{
      e => Future.successful( BadRequest(s"formWithErrors ${e.toString}") )
    }
}
def create_input() = silhouette.SecuredAction.async(BodyParsers.parse.json) { implicit request =>
  request.body.validate[StrategyInputRef].map{
    case entity => StrategyInputRefsDAOF.pull(entity).map { obj =>
       obj match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
        }
    }.recoverTotal{
      e => Future.successful( BadRequest(s"formWithErrors ${e.toString}") )
    }
}
def create_output() = silhouette.SecuredAction.async(BodyParsers.parse.json) { implicit request =>
  request.body.validate[StrategyOutputRef].map{
    case entity => StrategyOutputRefsDAOF.pull(entity).map { obj =>
       obj match {
            case -1 =>  Ok(Json.toJson(Map("failure" ->  s"Could not create ref ${entity}")))
            case id =>  {
              Ok(Json.toJson(Map("success" ->  id)))
            }
          }
        }
    }.recoverTotal{
      e => Future.successful( BadRequest(s"formWithErrors ${e.toString}") )
    }
}




def delete_base(base_id: Long) = silhouette.SecuredAction.async { implicit request =>
   StrategyBaseRefsDAOF.delete(base_id).map { res =>
     res match {
       case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
       case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
     }
   }
}

def delete_input(input_id: Long) = silhouette.SecuredAction.async { implicit request =>
  StrategyBaseRefsDAOF.delete(input_id).map { res =>
    res match {
      case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
      case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
    }
  }
}

def delete_output(output_id: Long) = silhouette.SecuredAction.async { implicit request =>
  StrategyBaseRefsDAOF.delete(output_id).map { res =>
    res match {
      case 0 =>  Ok(Json.toJson(Map("failure" -> "Entity has Not been deleted")))
      case x =>  Ok(Json.toJson(Map("success" -> s"Entity has been deleted (deleted $x row(s))")))
    }
  }
}












private def makeTopolog(ref: Int,
	front_elem_id: Option[Int],
	space_elem_id: Option[Int]):Int = {
    ReflectElemTopologDAO.pull_object(RefElemTopology(None,
  ref,
  front_elem_id,
  space_elem_id,
  "",
  None))
}
private def autoTracing() = {
	// TODO: State, switchers
}

}
