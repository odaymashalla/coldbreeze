package models.daos

import java.util.UUID

import com.mohiva.play.silhouette.api.LoginInfo
import models.User2

import scala.concurrent.Future

/**
 * Give access to the user object.
 */
trait UserDAO {

  /**
   * Finds a user by its user ID.
   *
   * @return The found users.
   */
  def findAll(): Future[Seq[User2]]

  def findPasswordHash(loginInfo: LoginInfo): Future[Option[DBPasswordInfo]]
  /**
   * Finds a user by its login info.
   *
   * @param loginInfo The login info of the user to find.
   * @return The found user or None if no user for the given login info could be found.
   */
  def find(loginInfo: LoginInfo): Future[Option[User2]]

  /**
   * Finds a user by its user ID.
   *
   * @param userID The ID of the user to find.
   * @return The found user or None if no user for the given ID could be found.
   */
  def find(userID: UUID): Future[Option[User2]]

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User2): Future[User2]
}
