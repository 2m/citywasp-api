package citywasp.api

import com.typesafe.config.Config
import com.ning.http.client.cookie.Cookie
import com.ning.http.client.Response
import dispatch._
import dispatch.Defaults._
import scala.concurrent.Future
import scala.collection.JavaConversions._

object RemoteCityWasp {

  implicit class RichResponse(r: Response) {
    def header(name: String): Option[String] = {
      val headers = r.getHeaders(name)
      if (headers == null) None else headers.headOption
    }
  }

  trait Common {
    def config: Config
    def request: Req = request("")
    def request(path: String) = url(config.getString("url") + path) <:< Map("User-Agent" -> config.getString("user-agent"))
  }

  case class RemoteCityWasp(config: Config) extends CityWasp with Common {
    def session = {
      val sessionCookieName = config.getString("session-cookie")
      val sessionCookie = for {
        response <- Http(request("/lt/auth/"))
      } yield response.getCookies.find(_.getName == sessionCookieName)

      sessionCookie.flatMap {
        case Some(c) => Future.successful(RemoteSession(config: Config, c))
        case None => Future.failed(new Error(s"Did not get a $sessionCookieName cookie."))
      }
    }
  }

  private case class RemoteSession(config: Config, sessionCookie: Cookie) extends Session with Common {
    def loginChallenge = {
      val Challenge = """(?s).*token]" value="([A-Za-z0-9]+)".*""".r
      Http(request("/lt/auth/") addCookie(sessionCookie) OK as.String).flatMap {
        case Challenge(token) => Future.successful(RemoteLoginChallenge(config, sessionCookie, token))
        case s => Future.failed(new Error(s"Did not get login challenge token in $s."))
      }
    }
  }

  private case class RemoteLoginChallenge(config: Config, sessionCookie: Cookie, challenge: String) extends LoginChallenge with Common {
    def login = {
      val credentials = Map(
        "login[_token]" -> challenge,
        "login[email]" -> config.getString("email"),
        "login[password]" -> config.getString("password")
      )
      val LoginSuccess = ".*showInfo.*".r
      val LoginFailure = ".*auth.*".r
      Http(request("/lt/auth/login/") << credentials addCookie(sessionCookie)).map(_.header("Location").headOption).flatMap {
        case Some("/lt/?showInfo=1") => Future.successful(RemoteLoggedIn(config, sessionCookie))
        case Some("/lt/auth/") => Future.failed(new Error(s"Unable to log in. Check username/password."))
        case _ => Future.failed(new Error(s"Error while logging in."))
      }
    }
  }

  private case class RemoteLoggedIn(config: Config, sessionCookie: Cookie) extends LoggedIn with Common {
    def currentCar = {
      val CarReserved = "(?s).*currentTime = ([0-9]+);.*car/unlock/([0-9]+).*".r
      val CarUnlocked = "(?s).*car/lock/([0-9]+).*".r
      val NoCar = "(?s).*Duomenų nėra.*".r
      Http(request / "lt" / "reservation" / "active" addCookie(sessionCookie) OK as.String).flatMap {
        case CarReserved(msLeft, carId) => Future.successful(Some(RemoteLockedCar(config, sessionCookie, carId)))
        case CarUnlocked(carId) => Future.successful(Some(RemoteUnlockedCar(config, sessionCookie, carId)))
        case NoCar() => Future.successful(None)
        case _ => Future.failed(new Error(s"Error while getting current car."))
      }
    }
  }

  private case class RemoteLockedCar(config: Config, sessionCookie: Cookie, carId: String) extends LockedCar with Common {
    def unlock =
      Http(request / "lt" / "car" / "unlock" / carId addCookie(sessionCookie)).map(_.header("Location")).flatMap {
        case Some("/lt/reservation/active") => Future.successful(())
        case _ => Future.failed(new Error(s"Error while unlocking current car."))
      }
  }

  private case class RemoteUnlockedCar(config: Config, sessionCookie: Cookie, carId: String) extends UnlockedCar with Common {
    def lock =
      Http(request / "lt" / "car" / "lock" / carId addCookie(sessionCookie)).map(_.header("Location").headOption).flatMap {
        case Some("/lt/") => Future.successful(())
        case _ => Future.failed(new Error(s"Error while locking current car."))
      }
  }

  def apply(config: Config) = RemoteCityWasp(config)

}

