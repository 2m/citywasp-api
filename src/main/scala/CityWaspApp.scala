package citywasp.api

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.ConfigFactory

import scala.util._

object CityWaspApp {
  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("CityWaspApp")
    implicit val mat = ActorMaterializer()

    try {
      run()
      scala.io.StdIn.readLine()
    } finally {
      sys.terminate()
    }
  }

  private def run()(implicit sys: ActorSystem, mat: Materializer) = {
    val config = ConfigFactory.load().getConfig("citywasp")

    import sys.dispatcher

    implicit val cw = RemoteCityWasp(config)
    val greeting = for {
      start <- CityWasp(cw)
      login <- start.login
      _ = println(s"Login: $login")
      availableCars <- login.availableCars
      _ = println(s"Parked cars: ${availableCars.toList}")
      carsDetails <- login.carsDetails
      _ = println(s"Cars details: ${carsDetails.toList}")
    } yield Done

    greeting.onComplete {
      case Success(s) => println(s)
      case Failure(ex) =>
        println(ex.getMessage)
        ex.printStackTrace()
    }
  }
}
