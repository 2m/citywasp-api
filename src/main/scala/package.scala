package citywasp

import scala.collection.immutable

package object api {
  type Seq[A] = immutable.Seq[A]
  val Seq = immutable.Seq
}
