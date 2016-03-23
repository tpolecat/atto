package atto.compat

import scala.language.higherKinds

trait NonEmptyListy[F[_]] {
  def cons[A](a: A, as: List[A]): F[A]
  def toList[A](as: F[A]): List[A]
}
