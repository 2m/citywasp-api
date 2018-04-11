package citywasp.api

import com.typesafe.config.ConfigFactory
import scala.util._

object CityWaspApp extends App {

  import scala.concurrent.ExecutionContext.Implicits.global

  val config = ConfigFactory.load().getConfig("citywasp")

  implicit val cw = RemoteCityWasp(config)
  val greeting = for {
    session    <- CityWasp.session
    _          = println(s"Session: $session")
    challenge  <- session.loginChallenge
    _          = println(s"Challenge: $challenge")
    login      <- challenge.login
    _          = println(s"Login: $login")
    parkedCars <- login.parkedCars
    _          = println(s"Parked cars: ${parkedCars.toList}")
    status <- login.currentCar.map {
               case Some(c: LockedCar)   => "car locked"
               case Some(c: UnlockedCar) => "car unlocked"
               case None                 => "no car"
             }
  } yield status

  greeting.onComplete {
    case Success(s) => println(s)
    case Failure(ex) =>
      println(ex.getMessage)
      ex.printStackTrace()
  }

  scala.io.StdIn.readLine()

}
