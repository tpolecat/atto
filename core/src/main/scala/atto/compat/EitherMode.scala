package atto.compat

import scala.language.higherKinds

trait EitherMode {
  type E[_, _]
  def  left[A, B](a: A): E[A, B]
  def right[A, B](b: B): E[A, B]
}
