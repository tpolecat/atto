package atto.compat

import atto._
import atto.parser.combinator._
import atto.syntax.parser._

import scala.language.higherKinds

import _root_.cats.{ Foldable, Functor, Monad, Monoid, SemigroupK }
import _root_.cats.data.{ Xor, NonEmptyList }

object cats extends CatsModes
               with CatsShims
               with CatsInstances

trait CatsModes {

  implicit val CatsEithery: Eithery[Xor] =
    new Eithery[Xor] {
      def  left[A, B](a: A): A Xor B = Xor.left(a)
      def right[A, B](b: B): A Xor B = Xor.right(b)
    }

  implicit val CatsNonEmptyListy: NonEmptyListy[NonEmptyList] =
    new NonEmptyListy[NonEmptyList] {
      def cons[A](a: A, as: List[A]): NonEmptyList[A] = NonEmptyList(a, as)
      def toList[A](as: NonEmptyList[A]): List[A] = as.head :: as.tail
    }

}

trait CatsShims {

  implicit def CatsFoldy[F[_]](implicit F: Foldable[F]) =
    new Foldy[F] {
      def toList[A](fa: F[A]) = F.toList(fa)
    }

}

trait CatsInstances {

  implicit val ParserMonad: Monad[Parser] =
    new Monad[Parser] {
      def pure[A](a: A): Parser[A] = ok(a)
      def flatMap[A,B](ma: Parser[A])(f: A => Parser[B]) = ma flatMap f
      override def map[A,B](ma: Parser[A])(f: A => B) = ma map f
      def tailRecM[A, B](a: A)(f: A => Parser[Either[A, B]]) = defaultTailRecM(a)(f)
    }

  implicit val ParserSemigroupK: SemigroupK[Parser] =
    new SemigroupK[Parser] {
      def combineK[A](a: Parser[A], b: Parser[A]): Parser[A] = a | b
    }

  implicit def ParserMonoid[A]: Monoid[Parser[A]] =
    new Monoid[Parser[A]] {
      def combine(s1: Parser[A], s2: Parser[A]): Parser[A] = s1 | s2
      val empty: Parser[A] = err("zero")
    }

  implicit val ParseResultFunctor: Functor[ParseResult] =
    new Functor[ParseResult] {
      def map[A,B](ma: ParseResult[A])(f: A => B) =
        ma map f
    }

}
