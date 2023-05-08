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

import cats.effect.IO
import sttp.tapir.DecodeResult

trait CatsEffectSupport {
  class DecodeResultEitherOps[L, R](r: DecodeResult[Either[L, R]]) {

    def load = IO
      .pure(r)
      .flatMap {
        case DecodeResult.Value(v) => IO.pure(v)
        case result                => IO.raiseError(new Error(s"Failure while decoding: $result"))
      }
      .flatMap { r =>
        IO.fromEither(r.left.map(error => new Error(s"Failure while parsing: $error")))
      }
  }

  implicit def toDecodeResultEitherOps[L, R](dr: DecodeResult[Either[L, R]]) = new DecodeResultEitherOps(dr)
}
