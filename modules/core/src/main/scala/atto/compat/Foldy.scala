package atto.compat

import scala.language.higherKinds

trait Foldy[F[_]] {
  def toList[A](fa: F[A]): List[A]
}
