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

import cats.implicits._
import ciris.ConfigDecoder
import ciris.ConfigError
import sttp.model.Uri

trait ConfigDecoders {
  implicit val sttpUriDecoder: ConfigDecoder[String, Uri] =
    ConfigDecoder[String].mapEither { (_, value) =>
      Uri.parse(value).left.map(s => ConfigError(s))
    }

  implicit def listDecoder[T](implicit decoder: ConfigDecoder[String, T]): ConfigDecoder[String, List[T]] =
    ConfigDecoder[String].mapEither { (key, value) =>
      value.split(',').map(decoder.decode(key, _)).toList.sequence
    }
}
