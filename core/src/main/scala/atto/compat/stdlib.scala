package atto.compat

import scala.language.higherKinds

object stdlib extends StdlibModes
                 with StdlibShims

trait StdlibModes {

  implicit val StdlibEitherMode =
    new EitherMode {
      type E[+A, +B] = Either[A, B]
      def  left[A, B](a: A): E[A, B] = Left(a)
      def right[A, B](b: B): E[A, B] = Right(b)
    }

  implicit val StdlibNelMode =
    new NelMode {
      type NEL[+A] = (A, List[A])
      def cons[A](a: A, as: List[A]): NEL[A] = (a, as)
      def toList[A](as: NEL[A]): List[A] = as._1 :: as._2
    }

}

trait StdlibShims {

  implicit def StdlibFoldableShim[F[A] <: Traversable[A]] =
    new FoldableShim[F] {
      def toList[A](fa: F[A]) = fa.toList
    }

}
