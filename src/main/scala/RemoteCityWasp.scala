package citywasp.api

import com.typesafe.config.Config
import com.ning.http.client.cookie.Cookie
import com.ning.http.client.Response
import dispatch._
import dispatch.Defaults._

import scala.concurrent.Future
import scala.collection.JavaConversions._
import java.util.concurrent.TimeUnit

import scala.util.matching.Regex

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
    def request(path: String) = url(config.getString("url") + path)
  }

  case class RemoteCityWasp(config: Config) extends CityWasp with Common {
    val waspHttp = Http.configure {
      _.setAllowPoolingConnections(config.getBoolean("http.connection-pooling"))
        .setConnectTimeout(
          config
            .getDuration("http.connection-timeout", TimeUnit.MILLISECONDS)
            .toInt
        )
        .setRequestTimeout(
          config
            .getDuration("http.request-timeout", TimeUnit.MILLISECONDS)
            .toInt
        )
        .setCompressionEnforced(config.getBoolean("http.compression"))
        .setUserAgent(config.getString("http.user-agent"))
    }

    def session = {
      val sessionCookieName = config.getString("session-cookie")
      val sessionCookie = for {
        response <- waspHttp(request("/lt/auth/"))
      } yield response.getCookies.find(_.getName == sessionCookieName)

      sessionCookie.flatMap {
        case Some(c) =>
          Future.successful(RemoteSession(waspHttp, config: Config, c))
        case None =>
          Future.failed(new Error(s"Did not get a $sessionCookieName cookie."))
      }
    }
  }

  private case class RemoteSession(waspHttp: Http, config: Config, sessionCookie: Cookie) extends Session with Common {
    def loginChallenge = {
      val Challenge = """(?s).*token]" value="([A-Za-z0-9_\-]+)".*""".r
      waspHttp(request("/lt/auth/") addCookie (sessionCookie) OK as.String)
        .flatMap {
          case Challenge(token) =>
            Future.successful(RemoteLoginChallenge(waspHttp, config, sessionCookie, token))
          case s =>
            Future.failed(new Error(s"Did not get login challenge token in $s."))
        }
    }
  }

  private case class RemoteLoginChallenge(waspHttp: Http, config: Config, sessionCookie: Cookie, challenge: String)
      extends LoginChallenge
      with Common {
    def login = {
      val credentials = Map(
        "login[_token]" -> challenge,
        "login[email]" -> config.getString("email"),
        "login[password]" -> config.getString("password")
      )
      val LoginSuccess = ".*showInfo.*".r
      val LoginFailure = ".*auth.*".r
      waspHttp(request("/lt/auth/login/") << credentials addCookie (sessionCookie))
        .map(_.header("Location").headOption)
        .flatMap {
          case Some("/lt/?showInfo=1") =>
            Future.successful(RemoteLoggedIn(waspHttp, config, sessionCookie))
          case Some("/lt/auth/") =>
            Future.failed(new Error(s"Unable to log in. Check username/password."))
          case _ => Future.failed(new Error(s"Error while logging in."))
        }
    }
  }

  private case class RemoteLoggedIn(waspHttp: Http, config: Config, sessionCookie: Cookie)
      extends LoggedIn
      with Common {
    def currentCar = {
      val CarReserved =
        "(?s).*currentTime = ([0-9]+);.*reservation/start/([0-9]+).*".r
      val CarUnlocked = "(?s).*reservation/stop/([0-9]+).*".r
      val NoCar = """(?s).*msg">[\s]*Duomen.*""".r
      waspHttp(request / "mobile" / "lt" / "reservation" / "active" addCookie (sessionCookie) OK as.String)
        .flatMap {
          case CarReserved(msLeft, carId) =>
            Future.successful(Some(RemoteLockedCar(waspHttp, config, sessionCookie, carId)))
          case CarUnlocked(carId) =>
            Future.successful(Some(RemoteUnlockedCar(waspHttp, config, sessionCookie, carId)))
          case NoCar() => Future.successful(None)
          case _ =>
            Future.failed(new Error(s"Error while getting current car."))
        }
    }

    def parkedCars = {
      val CarListing =
        """(?s)"id":([0-9]+),"licensePlate":"([A-Z0-9]+)","brand":"([A-Za-z0-9 ]+)","model":"([A-Za-z0-9 \\]+).*?"lat":([0-9\.]+),"lon":([0-9\.]+)""".r
      waspHttp(request / "mobile" / "lt" / "" addCookie (sessionCookie) OK as.String)
        .map(
          resp =>
            CarListing.findAllMatchIn(resp).toSeq.map { m =>
              ParkedCar(m.group(1).toInt, m.group(2), m.group(3), m.group(4), m.group(5).toDouble, m.group(6).toDouble)
          }
        )
    }
  }

  private case class RemoteLockedCar(waspHttp: Http, config: Config, sessionCookie: Cookie, carId: String)
      extends LockedCar
      with Common {
    def unlock =
      waspHttp(request / "mobile" / "lt" / "reservation" / "start" / carId addCookie (sessionCookie))
        .map(_.header("Location"))
        .flatMap {
          case Some("/mobile/lt/reservation/active") => Future.successful(())
          case _ =>
            Future.failed(new Error(s"Error while unlocking current car."))
        }
  }

  private case class RemoteUnlockedCar(waspHttp: Http, config: Config, sessionCookie: Cookie, carId: String)
      extends UnlockedCar
      with Common {
    def lock =
      waspHttp(request / "mobile" / "lt" / "reservation" / "stop" / carId addCookie (sessionCookie))
        .map(_.header("Location").headOption)
        .flatMap {
          case Some("/lt/") => Future.successful(())
          case _ =>
            Future.failed(new Error(s"Error while locking current car."))
        }
  }

  def apply(config: Config) = RemoteCityWasp(config)

}
