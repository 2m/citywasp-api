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

import cats.data.Validated
import com.monovore.decline.Argument
import sttp.model.Uri

trait DeclineSupport {
  implicit val configArgument: Argument[Uri] = new Argument[Uri] {

    def read(value: String) =
      Uri.parse(value) match {
        case Left(error) => Validated.invalidNel(error)
        case Right(uri)  => Validated.valid(uri)
      }

    def defaultMetavar = "uri"
  }
}
