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


class Application(override implicit val env: RuntimeEnvironment[DemoUser]) extends Controller with securesocial.core.SecureSocial[DemoUser] { // with Secured  {
  import play.api.Play.current
  /**
   * Returns the JavaScript router that the client can use for "type-safe" routes.
   * Uses browser caching; set duration (in seconds) according to your release cycle.
   * @param varName The name of the global variable, defaults to `jsRoutes`
   */
  def jsRoutes(varName: String = "jsRoutes") = Cached(_ => "jsRoutes", duration = 86400) {
    Action { implicit request =>
      Ok(
        Routes.javascriptRouter(varName)(
          routes.javascript.CustomLoginController.login//Application.login
          
          // TODO Add your routes here
        )
      ).as(JAVASCRIPT)
    }
  }





  import play.api.libs.json._
  import play.api.libs.functional.syntax._

  case class Person(name: String, age: Int, lovesChocolate: Option[Boolean])
  implicit val personReads = Json.reads[Person]
  //implicit val userReads = Json.reads[User]
  implicit val personWrites = Json.format[Person]
  //implicit val personWrites = Json.writes[Person]


/**
   * Log-in a user. Expects the credentials in the body in JSON format.
   *
   * Set the cookie [[AuthTokenCookieKey]] to have AngularJS set the X-XSRF-TOKEN in the HTTP
   * header.
   *
   * @return The token needed for subsequent requests
   */
  def login() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }
  def logout() = Action(parse.json) { implicit request =>
    
      Ok(views.html.index11())
    
  }


  def index = SecuredAction { implicit request =>
    Ok(views.html.index(request.user.main))
  }
  def app() = SecuredAction { implicit request =>
    Ok(views.html.app())
  }
  def whoami = SecuredAction { implicit request =>
    Ok(Json.toJson(Map("email" -> request.user.main.userId, "business" -> "0")))
  }

  // a sample action using an authorization implementation
  def onlyTwitter = SecuredAction(WithProvider("twitter")) { implicit request =>
    Ok("You can see this because you logged in using Twitter")
  }

  def linkResult = SecuredAction { implicit request =>
    Ok(views.html.linkResult(request.user))
  }

  /**
   * Sample use of SecureSocial.currentUser. Access the /current-user to test it
   */
  def currentUser = Action.async { implicit request =>
    import play.api.libs.concurrent.Execution.Implicits._
    SecureSocial.currentUser[DemoUser].map { maybeUser =>
      val userId = maybeUser.map(_.main.userId).getOrElse("unknown")
      Ok(s"Your id is $userId")
    }
  }


// An Authorization implementation that only authorizes uses that logged in using twitter
  case class WithProvider(provider: String) extends Authorization[DemoUser] {
    def isAuthorized(user: DemoUser, request: RequestHeader) = {
      user.main.providerId == provider
    }
  }
  def index11 = Action {
    Ok(views.html.index11())
  }
  /**
  * Mocks
  */
  def bprocesses = Action {
    Ok(Json.toJson(Array(4)))
  }
  import models.DAO.BProcessDTO
  import models.DAO.BPDAO
  implicit val BProcessDTOReads = Json.reads[BProcessDTO]
  implicit val BProcessDTOWrites = Json.format[BProcessDTO]

  def bprocess = Action {
    Ok(Json.toJson(BPDAO.getAll))
  }
  def create_bprocess1 = Action { implicit request =>
    println(request.body)
    Ok(Json.toJson(Array(BProcessDTO(Some(1), "12", 1, 1))))
  }
  def show_bprocess(id: Int) = Action { implicit request =>
    BPDAO.get(id) match {
      case Some(bprocess) => Ok(Json.toJson(bprocess))
      case _ => BadRequest(Json.obj("status" -> "Not found"))
    }
  }
  def create_bprocess = Action(BodyParsers.parse.json) { request =>
  val bpResult = request.body.validate[BProcessDTO]
  bpResult.fold(
    errors => {
      BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
    },
    bprocess => { 
      BPDAO.pull_object(bprocess)
      Ok(Json.obj("status" ->"OK", "message" -> ("Bprocess '"+bprocess.id+"' saved.") ))  
    }
  )
}
  def update_bprocess(id: Int) = Action(BodyParsers.parse.json) { implicit request =>
    val bpResult = request.body.validate[BProcessDTO]
  bpResult.fold(
    errors => {
      BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
    },
    bprocess => { 
      BPDAO.update(id, bprocess)
      Ok(Json.obj("status" ->"OK", "message" -> ("Bprocess '"+bprocess.title+"' saved.") ))  
    }
  )

  }
  def delete_bprocess = Action { implicit request => 
    println(request.body)
    Ok(Json.toJson(true))
  }
/**
For this Action we’ll need to define an implicit Reads[Place] to convert a JsValue to our model.

implicit val locationReads: Reads[Location] = (
  (JsPath \ "lat").read[Double] and
  (JsPath \ "long").read[Double]
)(Location.apply _)

implicit val placeReads: Reads[Place] = (
  (JsPath \ "name").read[String] and
  (JsPath \ "location").read[Location]
)(Place.apply _)

Next we’ll define the Action.

def savePlace = Action(BodyParsers.parse.json) { request =>
  val placeResult = request.body.validate[Place]
  placeResult.fold(
    errors => {
      BadRequest(Json.obj("status" ->"KO", "message" -> JsError.toFlatJson(errors)))
    },
    place => { 
      Place.save(place)
      Ok(Json.obj("status" ->"OK", "message" -> ("Place '"+place.name+"' saved.") ))  
    }
  )
}
*/
import models.DAO.UndefElement
import models.DAO.CompositeValues
implicit val CompositeVReads = Json.reads[CompositeValues]
implicit val CompositeVWrites = Json.format[CompositeValues]
implicit val UndefElementReads = Json.reads[UndefElement]
implicit val UndefElementWrites = Json.format[UndefElement]
  def bpElems(id: Int) = Action {
     Ok(Json.toJson(Array(
      UndefElement(Some(1), "title", "desc", 1, id, "b_type", "type_title", None, order = 1, Some(List(CompositeValues(Some("a"), Some("b"), Some(1) ))) ),
      UndefElement(Some(2), "title", "desc", 1, id, "b_type", "type_title", None, order = 2, Some(List(CompositeValues(Some("a"), Some("b"), Some(1) ))) ),
      UndefElement(Some(3), "title", "desc", 1, id, "b_type", "type_title", None, order = 3, Some(List(CompositeValues(Some("a"), Some("b"), Some(1) ))) )


      )))
  }
  def createBpElem(id: Int) = Action { implicit request =>
    println(request.body)
     Ok(Json.toJson(Array(UndefElement(Some(1), "title", "desc", 1, 1, "b_type", "type_title", None, order = 1, Some(List(CompositeValues(Some("a"), Some("b"), Some(1) ))) ))))
  }
  def updateBpElem(id: Int) = Action { implicit request => 
    println(request.body)
     Ok(Json.toJson(Array(UndefElement(Some(1), "title", "desc", 1, 1, "b_type", "type_title", None, order = 1, Some(List(CompositeValues(Some("a"), Some("b"), Some(1) ))) ))))
  }



  def bpstation = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
  def bpstationLogger = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
  def bplogger = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
  def bpSpaceElems = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
  def bpSpaces = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
  def bpThreads = Action {
    Ok(Json.toJson(Array(Person("xxx", 12, None ))))
  }
}

trait SubdomainController extends Controller {
  def WithSubdomain(f: => String => Request[AnyContent] => Result) = Action { implicit request =>
    val splitDomain = request.domain.split("\\.")

    if (splitDomain.length < 2) {
      BadRequest("Domain not found!")
    } else {
      f(splitDomain.head)(request)
    }
  }
}