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

import sttp.model.HeaderNames
import sttp.tapir._
import sttp.tapir.codec.refined._
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._

import lt.dvim.citywasp.api.Model._

object Api {
  private val api =
    endpoint
      .in(header(HeaderNames.Accept, "application/json"))
      .in(header(HeaderNames.AcceptLanguage, "en-US"))
      .in(header[AppVersion]("app-version"))
      .in(header[Country]("country"))
      .in(header[Instant]("datetime"))
      .in(header[UUID]("deviceid"))
      .in(header("software", "0"))
      .in(header[Token]("token"))
      .in(header("username", ""))
      .mapInTo(Params.apply _)
      .in("api")
      .errorOut(stringBody)

  object AvailableServices {
    val Get = api.get.in("AvailableServices" / "Get").out(jsonBody[List[Service]])
  }

  object CarsLive {
    val GetAvailableCars = api.get.in("CarsLive" / "GetAvailableCars").out(jsonBody[List[Car]])
  }
}
