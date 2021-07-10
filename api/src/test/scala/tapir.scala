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

import sttp.tapir.DecodeResult

trait DecodeResultOps {
  class DecodeResultEitherOps[L, R](r: DecodeResult[Either[L, R]]) {
    def orThrow = r match {
      case DecodeResult.Value(v) =>
        v match {
          case Right(r)      => r
          case Left(failure) => throw new Error(s"Failure while parsing: $failure")
        }
      case result => throw new Error(s"Failure while decoding: $result")
    }
  }
  implicit def toDecodeResultEitherOps[L, R](dr: DecodeResult[Either[L, R]]) = new DecodeResultEitherOps(dr)
}
