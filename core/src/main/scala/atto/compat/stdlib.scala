package atto.compat

import scala.language.higherKinds

object stdlib extends StdlibModes
                 with StdlibShims

trait StdlibModes {

  implicit val StdlibEithery: Eithery[Either] =
    new Eithery[Either] {
      def  left[A, B](a: A): Either[A, B] = Left(a)
      def right[A, B](b: B): Either[A, B] = Right(b)
    }

  implicit val StdlibNonEmptyListy: NonEmptyListy[λ[α => (α, List[α])]] =
    new NonEmptyListy[λ[α => (α, List[α])]] {
      def cons[A](a: A, as: List[A]): (A, List[A]) = (a, as)
      def toList[A](as: (A, List[A])): List[A] = as._1 :: as._2
    }

}

trait StdlibShims {

  implicit def StdlibFoldy[F[A] <: Traversable[A]] =
    new Foldy[F] {
      def toList[A](fa: F[A]) = fa.toList
    }

}
