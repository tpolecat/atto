package atto.compat

import scala.language.higherKinds

trait FoldableShim[F[_]] {
  def toList[A](fa: F[A]): List[A]
}
