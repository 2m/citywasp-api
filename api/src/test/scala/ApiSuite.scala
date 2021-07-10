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

import java.nio.file.Files
import java.nio.file.Paths

import eu.timepit.refined.auto._
import sttp.client3.testing.SttpBackendStub
import sttp.model.Header
import sttp.model.Uri
import sttp.tapir.client.sttp.SttpClientInterpreter

import lt.dvim.citywasp.api.Api
import lt.dvim.citywasp.api.Model._

class ApiSuite extends munit.FunSuite with DecodeResultOps {

  val uri = Some(Uri.unsafeParse("test.lt"))
  val params = Params.default
  val expectedHeaders = Seq(
    Header("Accept-Encoding", "gzip, deflate"),
    Header("Accept", "application/json"),
    Header("Accept-Language", "en-US"),
    Header("app-version", "9.9.9"),
    Header("country", "LT"),
    Header("datetime", params.datetime.getEpochSecond().toString),
    Header("deviceid", params.deviceId.toString),
    Header("software", "0"),
    Header("token", params.token),
    Header("username", "")
  )

  test("Api.AvailableServices.Get - request") {
    val request = SttpClientInterpreter().toRequest(Api.AvailableServices.Get, uri)
    val prepared = request(params)
    assertEquals(prepared.uri.toString, "test.lt/api/AvailableServices/Get")
    assertEquals(prepared.headers, expectedHeaders)
  }

  test("Api.AvailableServices.Get - response") {
    val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatches(_ => true)
      .thenRespond(Files.readString(Paths.get(getClass.getResource("/services.json").toURI())))
    val request = SttpClientInterpreter().toRequest(Api.AvailableServices.Get, uri)
    val prepared = request(params)
    val response = prepared.send(testingBackend)

    val services = response.body.orThrow
    assertEquals(services, List(Service(21, "service1", false), Service(19, "service2", false)))
  }

  test("Api.CarsLive.GetAvailableCars - request") {
    val request = SttpClientInterpreter().toRequest(Api.CarsLive.GetAvailableCars, uri)
    val prepared = request(params)
    assertEquals(prepared.uri.toString, "test.lt/api/CarsLive/GetAvailableCars")
    assertEquals(prepared.headers, expectedHeaders)
  }

  test("Api.CarsLive.GetAvailableCars - response") {
    val testingBackend = SttpBackendStub.synchronous
      .whenRequestMatches(_ => true)
      .thenRespond(Files.readString(Paths.get(getClass.getResource("/cars.json").toURI())))
    val request = SttpClientInterpreter().toRequest(Api.CarsLive.GetAvailableCars, uri)
    val prepared = request(params)
    val response = prepared.send(testingBackend)

    val cars = response.body.orThrow
    assertEquals(
      cars,
      List(
        Car(
          1369,
          11,
          BigDecimal("1.1"),
          BigDecimal("2.2"),
          BigDecimal(0.27),
          false,
          true,
          68,
          "address1",
          None
        ),
        Car(
          1370,
          11,
          BigDecimal("3.3"),
          BigDecimal("4.4"),
          BigDecimal("0.27"),
          false,
          true,
          52,
          "address2",
          Some("city2")
        )
      )
    )
  }

}
