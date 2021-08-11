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

import java.time.Instant
import java.util.UUID

import scala.util.Random

import eu.timepit.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string._
import io.circe.generic.extras._
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir._

object Model extends CarModel with ServiceModel {
  type AppVersion = String Refined MatchesRegex["[0-9]\\.[0-9]\\.[0-9]"]

  case class Country(code: String)
  object Country {
    implicit val codec: Codec[String, Country, TextPlain] =
      Codec.string.mapDecode(_ => ???)(_.code)

    def fromUri(uri: sttp.model.Uri) = uri.host
      .map(_.split("\\.").last.toUpperCase())
      .map(Country.apply)
      .getOrElse(throw new Error(s"Unable to parse Country from $uri"))
  }
  val Lt = Country("LT")
  val Lv = Country("LV")
  val Ee = Country("EE")

  implicit val instantCodec: Codec[String, Instant, TextPlain] =
    Codec.string.mapDecode(_ => ???)(_.getEpochSecond().toString)

  type TokenRegex = "[0-9a-f]{64}"
  type Token = String Refined MatchesRegex[TokenRegex]
  object Token {
    def random(): Token = refineV[MatchesRegex[TokenRegex]](
      Random.alphanumeric.filter(c => c.isDigit || ('a' to 'f').contains(c)).take(64).mkString("")
    ).fold(err => throw new Error(err), identity)
  }

  case class Params(appVersion: AppVersion, country: Country, datetime: Instant, deviceId: UUID, token: Token)
  object Params {
    def default = Params(
      "9.9.9",
      Lt,
      Instant.now(),
      UUID.randomUUID(),
      Token.random()
    )
  }
}

sealed trait ServiceModel {
  implicit val serviceConfig: Configuration = Configuration.default.copy(transformMemberNames = _.capitalize)
  @ConfiguredJsonCodec case class Service(serviceId: Int, serviceName: String, isVisible: Boolean)
}

sealed trait CarModel {
  implicit val carConfig: Configuration = Configuration.default.withSnakeCaseMemberNames
  @ConfiguredJsonCodec case class Car(
      id: Int,
      serviceId: Int,
      lat: BigDecimal,
      long: BigDecimal,
      price: BigDecimal,
      isElectric: Boolean,
      isCargo: Boolean,
      fuelLevel: Int,
      address: Option[String],
      city: Option[String]
  )
}
