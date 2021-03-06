
package formatters

import play.api.libs.json._
import play.api.libs.functional.syntax._
import com.mohiva.play.silhouette.api.LoginInfo

import models._

/**
 * This object contains all format for User class
 */
object BaseInfoFormats {

  val storageFormat = (
    (__ \ "firstName").formatNullable[String] ~
    (__ \ "lastName").formatNullable[String] ~
    (__ \ "fullName").formatNullable[String] ~
    (__ \ "gender").formatNullable[String])(BaseInfo2.apply _, unlift(BaseInfo2.unapply _))

  val restFormat = storageFormat

}
