/*
 * Copyright 2015 github.com/2m/citywasp-api/contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package citywasp.api

import scala.util._

import akka.Done
import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}

import com.typesafe.config.ConfigFactory

object CityWaspApp {
  def main(args: Array[String]): Unit = {
    implicit val sys = ActorSystem("CityWaspApp")
    implicit val mat = ActorMaterializer()

    try {
      run()
      scala.io.StdIn.readLine()
    } finally sys.terminate()
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
