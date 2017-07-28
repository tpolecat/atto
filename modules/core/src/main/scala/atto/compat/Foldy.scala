package atto.compat

import scala.List
import scala.language.higherKinds

trait Foldy[F[_]] {
  def toList[A](fa: F[A]): List[A]
}
