package controllers
import play.api._
import play.api.mvc._
import play.twirl.api.Html

//{Action, Controller}
import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.cache._

import views._
import models.User
import service.DemoUser
import securesocial.core._
/**
 * Created by Sobolev on 22.07.2014.
 */
class ProcessController(override implicit val env: RuntimeEnvironment[DemoUser]) extends Controller with securesocial.core.SecureSocial[DemoUser] {
  import play.api.Play.current
  
  def index() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }
  def newone() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }
  def create() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }
  def update() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }
  def destroy() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }

  def getStations() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }


 def getLoggers() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }

 def getElements() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }
 def getSpaces() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }
 def getSpaceElements() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }

 /**
  * Service actions
  */
 def haltProcess() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }
 def run() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }

 def inputCall() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }

 def sharing() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
 }


}