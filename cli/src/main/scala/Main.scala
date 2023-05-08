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

package lt.dvim.citywasp.cli

import cats.data.NonEmptyList
import cats.effect.ExitCode
import cats.effect.IO
import cats.effect.IOApp
import cats.implicits._
import com.monovore.decline.refined._
import sttp.client3.http4s.Http4sBackend
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter

import lt.dvim.citywasp.api.Api
import lt.dvim.citywasp.api.Model._

sealed trait CommandAction {
  def run(): IO[ExitCode]
}

final case class Cars(appVersion: AppVersion, uris: NonEmptyList[Uri]) extends CommandAction with CatsEffectSupport {

  val backend = Http4sBackend.usingDefaultBlazeClientBuilder[IO]()

  private def cars(appVersion: AppVersion)(uri: Uri) = {
    val params = Params.default.copy(appVersion = appVersion, country = Country.fromUri(uri))
    val servicesRequest = SttpClientInterpreter().toRequest(Api.AvailableServices.Get, Some(uri)).apply(params)
    val carsRequest = SttpClientInterpreter().toRequest(Api.CarsLive.GetAvailableCars, Some(uri)).apply(params)
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

  def run(): IO[ExitCode] =
    for {
      result <- uris.map(cars(appVersion)).combineAll
      _ = print(
        result.toList
          .sortBy(_._2)
          .map { case (car, count) => s"'$car': $count" }
          .mkString("\n")
      )
    } yield ExitCode.Success
}

final case class Services(appVersion: AppVersion, uris: NonEmptyList[Uri])
    extends CommandAction
    with CatsEffectSupport {

  val backend = Http4sBackend.usingDefaultBlazeClientBuilder[IO]()

  private def services(appVersion: AppVersion)(uri: Uri) = {
    val params = Params.default.copy(appVersion = appVersion, country = Country.fromUri(uri))
    val servicesRequest = SttpClientInterpreter().toRequest(Api.AvailableServices.Get, Some(uri)).apply(params)
    for {
      services <- backend.use(servicesRequest.send(_)).flatMap(_.body.load)
    } yield services
  }

  def run(): IO[ExitCode] =
    for {
      result <- uris.map(services(appVersion)).combineAll
      _ = print(
        result.toList
          .sortBy(_.serviceName)
          .map(s => s"'${s.serviceName}': ${s.serviceId}")
          .mkString("\n")
      )
    } yield ExitCode.Success
}

import com.monovore.decline._

object Commands extends DeclineSupport {
  val opts = (
    Opts
      .option[AppVersion]("app-version", short = "a", help = "App version"),
    Opts.options[Uri]("uri", help = "Backend URI. Can be specified multiple times.")
  )

  val commands = {

    val cars =
      Command(
        name = "cars",
        header = "Prints out a list of all of the cars currently known."
      )(opts.mapN(Cars))

    val services =
      Command(
        name = "services",
        header = "Prints out a list of all of the services currently offered."
      )(opts.mapN(Services))

    Opts
      .subcommand(cars)
      .orElse(Opts.subcommand(services))
  }
}

object Citywasp extends IOApp {
  def run(args: List[String]): IO[ExitCode] = {
    val command: Command[CommandAction] = Command(
      name = "citywasp-cli",
      header = "The CLI for the car sharing application",
      helpFlag = true
    )(Commands.commands)

    command.parse(args) match {
      case Right(cmd) =>
        cmd.run()
      case Left(e) =>
        IO(println(e.toString())) *> IO(ExitCode.Error)
    }
  }
}
