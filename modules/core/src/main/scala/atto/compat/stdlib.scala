package atto.compat

import scala.language.higherKinds

object stdlib extends StdlibModes
                 with StdlibShims

trait StdlibModes

trait StdlibShims {

  implicit def StdlibFoldy[F[A] <: Traversable[A]] =
    new Foldy[F] {
      def toList[A](fa: F[A]) = fa.toList
    }

}
