package controllers
import utils.auth.DefaultEnv

import play.api.Play.current
import play.api.libs.mailer._

import play.api._
import play.api.mvc._
import play.twirl.api.Html

import play.api.http.MimeTypes
import play.api.libs.json._
import play.api.cache._
import play.api.data._
import play.api.data.Forms._
import play.api.data.format.Formats
import play.api.data.format.Formatter
import play.api.data.FormError


import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import forms._
import models.User
import play.api.i18n.{ I18nSupport, MessagesApi }
import models._
import models.daos._





import java.io.File
import org.apache.commons.mail.EmailAttachment





import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import models.User
import models.services.UserService
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import scala.concurrent.Future

import javax.inject.Inject
import scala.concurrent.Future

import play.api.mvc._
import play.api.libs.ws._
import play.api.http.{ HttpEntity, LazyHttpErrorHandler, HttpErrorHandler, ContentTypes }
import akka.stream.scaladsl._
import play.api.libs.streams._
import akka.util._

case class FileInstance(fileName: String, fileUrl: String,
  file: Option[models.DAO.File] = None,
  launchFile: Option[models.DAO.LaunchFile] = None

)
case class FilesContainer(files: Seq[FileInstance])

class FileUploadController @Inject() (
  mailerClient: MailerClient,
  ws: WSClient,
  val messagesApi: MessagesApi,
  silhouette: Silhouette[DefaultEnv],
  socialProviderRegistry: SocialProviderRegistry)(implicit val mat: akka.stream.Materializer) 
  extends Controller with I18nSupport {

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Awaitable, Await, Future}
import scala.util.Try
import sys.process._

implicit val FileHandler = Json.reads[models.DAO.File]
implicit val FileFormat = Json.format[models.DAO.File]
implicit val LaunchFileHandler = Json.reads[models.DAO.LaunchFile]
implicit val LaunchFileFormat = Json.format[models.DAO.LaunchFile]



implicit val FileInstanceHandler = Json.reads[FileInstance]
implicit val FileInstanceFormat = Json.format[FileInstance]

implicit val FilesContainerFormat = Json.format[FilesContainer]
implicit val FilesContainerReader = Json.reads[FilesContainer]

//implicit val SeqFileInstanceHandler = Json.reads[Seq[FileInstance]]
//implicit val SeqFileInstanceFormat = Json.format[Seq[FileInstance]]


def await[T](a: Awaitable[T])(implicit ec: ExecutionContext) = Await.result(a, Duration.Inf)
def awaitAndPrint[T](a: Awaitable[T])(implicit ec: ExecutionContext) = println(await(a))

val azure = _root_.util.AzureUploadHandler.apply()


def downloadFile(fileName: String, hash: String, urlParams: String) = silhouette.SecuredAction.async { implicit request =>
  "http://minorityapp.blob.core.windows.net/minority-uploads/"

// http://127.0.0.1:9000/downloadFile/landing_page.png+61353b95-b825-40b6-97a0-c935b9937911/sig=Va2woPzZ6uup5PNVdxwTCTNPb6LfZjpiB4Y6O9psEbM%3D&se=2016-05-05T11%3A11%3A22Z&sv=2015-04-05&sp=rwdl&sr=b
// https://minorityapp.blob.core.windows.net/minority-uploads/landing_page.png+61353b95-b825-40b6-97a0-c935b9937911

 val resultFuture = ws.url(s"http://minorityapp.blob.core.windows.net/minority-uploads/$fileName+$hash?$urlParams").getStream

 //val dataEnumeratorFuture = resultFuture.map(stream => stream._2)
 //dataEnumeratorFuture.map(Ok.chunked(_))

 //OR
 resultFuture.map {
   case (rs, streamm) => {
     val stream = streamm.map(c => ByteString.fromArray(c))
     val source = Source.fromPublisher(Streams.enumeratorToPublisher(stream))
     val length = rs.headers.get("Content-Length").map(_.head).get
     val cType = rs.headers.get("Content-Type").map(_.head).getOrElse("binary/octet-stream")
     val streamed = HttpEntity.Streamed(source, Some(length.toLong), Some(cType) )

     Result(
       header = ResponseHeader(
         status = OK,
         headers = Map(
           CONTENT_LENGTH -> length,
           CONTENT_DISPOSITION -> s"""attachment; filename="$fileName"""",
           CONTENT_TYPE -> cType)
       ),
       body = streamed
     )
    } 
 }


}



def allFiles = silhouette.SecuredAction.async { implicit request =>
  models.DAO.FilesDAO.getAll.flatMap { files =>
    val filesUrlF: Future[Seq[FileInstance]] = azure.getAll()//Seq(FileInstance("fileName", "url" ) )
    filesUrlF.map { filesUrl =>
      Ok(Json.toJson( FilesContainer(filesUrl)) )
    }
  }
}

def allFilesForLaunch(launch_id: Int)  = silhouette.SecuredAction.async { implicit request =>
  val launchFilesF = models.DAO.LaunchFilesDAO.findByLaunchId(launch_id)
  launchFilesF.flatMap { launchFiles =>
    val ids = launchFiles.map(_.fileId)
    models.DAO.FilesDAO.getByIds(ids).flatMap { files =>
      println("files"+ files.length)
      val azureIds = files.map(_.azureId)
      azureIds.map(println)
      val filesUrlF: Future[Seq[FileInstance]] = azure.getLinkById(azureIds, files, launchFiles) //Seq(FileInstance("fileName", "url" ) )
      filesUrlF.map { filesUrl =>
        Ok(Json.toJson( FilesContainer(filesUrl)) )
      }
    }

  }
}

def allFilesForLaunchElement(launch_id: Int, element_id: Int)  = silhouette.SecuredAction.async { implicit request =>
  val launchFilesF = models.DAO.LaunchFilesDAO.findByLaunchElementId(launch_id, element_id)
  launchFilesF.flatMap { launchFiles =>
    val ids = launchFiles.map(_.fileId)
    models.DAO.FilesDAO.getByIds(ids).flatMap { files =>
      val filesUrlF: Future[Seq[FileInstance]] = azure.getLinkById(files.map(_.azureId) )//Seq(FileInstance("fileName", "url" ) )
      filesUrlF.map { filesUrl =>
        Ok(Json.toJson( FilesContainer(filesUrl)) )
      }
    }

  }
}




def upload = silhouette.SecuredAction(parse.maxLength(10 * 1024 * 1024, parse.multipartFormData)) { request =>
  val workbench = 0
  val description = ""
  request.body match {
          case Left(MaxSizeExceeded(length)) => BadRequest("Your file is too large, we accept just " + length + " bytes!")
          case Right(multipartForm) => {
            println("multipartForm"+multipartForm)
            multipartForm.files.map { file =>
                import java.io.File
                val filename = file.filename
                val contentType = file.contentType
                file.ref.moveTo(new File(s"bp-web2/tmp_uploads/$filename"))
                azure.upload(file.filename, workbench, description)
              }
              Ok("File uploaded")
          }
  }

}




def uploadLaunchFile(launch_id: Int, element_id: Option[Int]) = silhouette.SecuredAction(parse.maxLength(10 * 1024 * 1024, parse.multipartFormData)) { request =>
  val workbench = 0
  val description = ""
  request.body match {
          case Left(MaxSizeExceeded(length)) => BadRequest("Your file is too large, we accept just " + length + " bytes!")
          case Right(multipartForm) => {
            println("multipartForm"+multipartForm)
            multipartForm.files.map { file =>
                import java.io.File
                val filename = file.filename
                val contentType = file.contentType
                file.ref.moveTo(new File(s"bp-web2/tmp_uploads/$filename"))
                azure.uploadLaunchFile(file.filename, workbench, description, launch_id, element_id)
              }
              Ok("File uploaded")
          }
  }
}


def deleteLaunchFile(launchFileId: Long) = silhouette.SecuredAction.async { request =>
  models.DAO.LaunchFilesDAO.findById(launchFileId).flatMap { launchFileOpt =>
    launchFileOpt match {
      case Some(l) => {
        models.DAO.LaunchFilesDAO.delete(l.id.get).flatMap { c =>
          models.DAO.FilesDAO.findById(l.fileId).flatMap { fi =>
          models.DAO.FilesDAO.delete(l.fileId).map { a =>
            if (!fi.get.external) { // remove only in azure storage
              azure.removeFileById(fi.get.fileName+"+"+fi.get.azureId, fi.get.azureId)
            }
            Ok("deleted")
          }
          }
        }
      }
      case _ => {
        Future.successful( Ok("good") )
      }
    }

  }
}


}
