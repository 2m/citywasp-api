/*
 * Copyright 2015 github.com/2m/citywasp-api/graphs/contributors
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

package lt.dvim.citywasp.api

import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits._
import ciris._
import ciris.refined._
import sttp.client3.http4s.Http4sBackend
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter

import lt.dvim.citywasp.api.Model._

object Main extends IOApp with ConfigDecoders with CatsEffectSupport {
  final case class Config(
      uris: List[Uri],
      appVersion: AppVersion
  )

  val config = (prop("uris").as[List[Uri]], prop("app").as[AppVersion]).parMapN(Config)

  val backend = Http4sBackend.usingDefaultBlazeClientBuilder[IO]()

  private def cars(appVersion: AppVersion)(uri: Uri) = {
    val params = Params.default.copy(appVersion = appVersion, country = Country.fromUri(uri))
    val servicesRequest = SttpClientInterpreter.toRequest(Api.AvailableServices.Get, Some(uri)).apply(params)
    val carsRequest = SttpClientInterpreter.toRequest(Api.CarsLive.GetAvailableCars, Some(uri)).apply(params)
    for {
      services <- backend.use(servicesRequest.send(_)).flatMap(_.body.load)
      cars <- backend.use(carsRequest.send(_)).flatMap(_.body.load)
    } yield {
      val allServices = Map.from(services.map(_.serviceName).map(_ -> 0))
      val carCounts = cars
        .flatMap { car =>
          services.find(_.serviceId == car.serviceId).map(_.serviceName)
        }
        .groupBy(identity)
        .view
        .mapValues(_.size)
        .toMap

      allServices.combine(carCounts)
    }

  }

  def run(args: List[String]): IO[ExitCode] =
    for {
      config <- config.load[IO]
      result <- config.uris.map(cars(config.appVersion)).combineAll
      _ = print(
        result.toList
          .sortBy(_._2)
          .map { case (car, count) => s"'$car': $count" }
          .mkString("\n")
      )
    } yield ExitCode.Success
}
