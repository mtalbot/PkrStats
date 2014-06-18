package services

import akka.actor.Actor
import forms.AuthenticateForm.AuthToken
import data.dao.Operation
import models.Player
import com.google.api.client.googleapis.auth.oauth2._
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Akka
import akka.actor.ActorRef
import data.dao.PlayerDAO
import play.api.Play.current
import akka.actor.Props
import akka.pattern.{ ask, pipe }
import models.AuthenticationType
import components.DbTimeout
import com.google.api.services.plus.Plus
import models.Player
import org.joda.time.DateTime
import scala.concurrent.Await

object Login {
  case class DoLogin(auth: AuthToken) extends Operation[Player]

  class UserAuthError(reason: String) extends Exception(reason)
  class MismatchedUserID extends Exception
  class MismatchedClientID extends Exception
}

class Login extends Actor with DbTimeout {
  private val CLIENT_ID = "247086175364-m66iououftl5irrphi3ktcrgtcdt3rn8.apps.googleusercontent.com"
  private val CLIENT_SECRET = "llqtpK-Aw-TJRGiZfUATHcqb"
  private val APPLICATION_NAME = "Pkr Stats"
  private val TRANSPORT = new NetHttpTransport()
  private val JSON_FACTORY = new JacksonFactory()
  private val playerDAO: ActorRef = Akka.system.actorOf(Props[PlayerDAO])

  def receive = {
    case Login.DoLogin(AuthToken(id, code, "g+")) => {
      val returnPath = sender
      Future {
        new GoogleAuthorizationCodeTokenRequest(TRANSPORT, JSON_FACTORY,
          CLIENT_ID, CLIENT_SECRET, code, "postmessage").execute()
      }.map { token =>
        (token.
          parseIdToken().
          getPayload().
          getSubject(), token)
      }.map { token =>
        (token, PlayerDAO.FindPlayerByAuthID(token._1, AuthenticationType.GooglePlus))
      }.flatMap { operation =>
        (playerDAO ? operation._2).map {
          result => (operation._1, operation._2.getResult(result))
        }
      }.flatMap {
        case (token, None) => {
          val credential = new GoogleCredential.Builder()
            .setJsonFactory(JSON_FACTORY)
            .setTransport(TRANSPORT)
            .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
            .build()
            .setFromTokenResponse(JSON_FACTORY.fromString(
              token._2.toString(), classOf[GoogleTokenResponse]))

          val service = new Plus.Builder(TRANSPORT, JSON_FACTORY, credential).
            setApplicationName(APPLICATION_NAME).
            build

          Future {
            service.
              people().
              get("me").
              execute()
          }.map { profile =>
            Player(-1,
              profile.getDisplayName(),
              List(profile.getNickname()).filterNot(_ == null),
              Some(token._1),
              Some(token._2.getRefreshToken),
              Some(DateTime.now().plusSeconds(token._2.getExpiresInSeconds.intValue)),
              Some(AuthenticationType.GooglePlus))
          }.map { player =>
            PlayerDAO.Insert(player)
          }.flatMap { operation =>
            (playerDAO ? operation).map { id =>
              PlayerDAO.Get(operation.getResult(id))
            }
          }.flatMap { operation =>
            (playerDAO ? operation).map { player => operation.getResult(player) }
          }
          //we need to create a player
        }
        case (token, Some(player)) => Future { player }
      }.map { player =>
        returnPath ! player
      }
    }
  }
}