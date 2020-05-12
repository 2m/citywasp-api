package citywasp.api

import com.typesafe.config.Config

import scala.concurrent.Future
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.marshalling.Marshaller._
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.{OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe._

object RemoteCityWasp {
  private object RemoteCityWasp extends FailFastCirceSupport {
    case class AuthResponse(accessToken: String)

    implicit val decodeAuthResponse = new Decoder[AuthResponse] {
      final def apply(c: HCursor) =
        for {
          accessToken <- c.downField("access_token").as[String]
        } yield AuthResponse(accessToken)
    }
  }

  private case class RemoteCityWasp()(implicit sys: ActorSystem, mat: Materializer, config: Config) extends CityWasp {
    import RemoteCityWasp._
    import sys.dispatcher

    def login: Future[LoggedIn] = {
      val uri = Uri(config.getString("url.auth")).withPath(Path / "token")

      val form = FormData(
        Map(
          "grant_type" -> "password",
          "username" -> config.getString("email"),
          "password" -> config.getString("password")
        )
      )

      for {
        req <- Marshal((HttpMethods.POST, uri, form)).to[HttpRequest]
        res <- Http().singleRequest(req)
        auth <- Unmarshal(res.entity).to[AuthResponse]
      } yield RemoteLoggedIn(auth)
    }
  }

  private object RemoteLoggedIn extends FailFastCirceSupport {
    implicit val decodeCar = new Decoder[Car] {
      final def apply(c: HCursor) =
        for {
          id <- c.downField("id").as[Int]
          lat <- c.downField("lat").as[Double]
          lon <- c.downField("long").as[Double]
        } yield Car(id, lat, lon)
    }

    implicit val decodeCarDetails = new Decoder[CarDetails] {
      final def apply(c: HCursor) =
        for {
          id <- c.downField("id").as[Int]
          licensePlate <- c.downField("license_plate").as[String]
          brand <- c.downField("make").as[String]
          model <- c.downField("model").as[String]
        } yield CarDetails(id, licensePlate, brand, model)
    }
  }

  private case class RemoteLoggedIn(auth: RemoteCityWasp.AuthResponse)(implicit
      sys: ActorSystem,
      mat: Materializer,
      config: Config
  ) extends LoggedIn {
    import RemoteLoggedIn._
    import sys.dispatcher

    override def availableCars: Future[Seq[Car]] = {
      val uri = Uri(config.getString("url.app")).withPath(Path / "api" / "CarsLive" / "GetAvailableCars")

      for {
        req <-
          Marshal(uri)
            .to[HttpRequest]
            .map(
              _.addCredentials(OAuth2BearerToken(auth.accessToken))
                .addHeader(RawHeader("App-Version", config.getString("app-version")))
            )
        res <- Http().singleRequest(req)
        cars <- Unmarshal(res.entity).to[Seq[Car]]
      } yield cars
    }

    override def carsDetails: Future[Seq[CarDetails]] = {
      val uri = Uri(config.getString("url.app")).withPath(Path / "api" / "CarsLive" / "GetCarsDetails")

      for {
        req <-
          Marshal(uri)
            .to[HttpRequest]
            .map(
              _.addCredentials(OAuth2BearerToken(auth.accessToken))
                .addHeader(RawHeader("App-Version", config.getString("app-version")))
            )
        res <- Http().singleRequest(req)
        details <- Unmarshal(res.entity).to[Seq[CarDetails]]
      } yield details
    }
  }

  def apply(config: Config)(implicit sys: ActorSystem, mat: Materializer): CityWasp =
    RemoteCityWasp()(sys, mat, config)
}
