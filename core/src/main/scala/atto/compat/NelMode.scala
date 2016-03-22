package atto.compat

import scala.language.higherKinds

trait NelMode {
  type NEL[_]
  def cons[A](a: A, as: List[A]): NEL[A]
  def toList[A](as: NEL[A]): List[A]
}
