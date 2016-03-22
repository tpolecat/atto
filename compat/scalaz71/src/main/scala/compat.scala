package atto.compat

import atto._
import atto.parser.combinator._
import atto.syntax.parser._

import scala.language.higherKinds

import _root_.scalaz._, Scalaz._

object scalaz extends ScalazModes
                 with ScalazShims
                 with ScalazInstances

trait ScalazModes {

  implicit val ScalazEitherMode =
    new EitherMode {
      type E[A, B] = A \/ B
      def  left[A, B](a: A): E[A, B] = \/.left(a)
      def right[A, B](b: B): E[A, B] = \/.right(b)
    }

  implicit val ScalazNelMode =
    new NelMode {
      type NEL[A] = NonEmptyList[A]
      def cons[A](a: A, as: List[A]): NEL[A] = NonEmptyList(a, as: _*)
      def toList[A](as: NEL[A]): List[A] = as.list
    }

}

trait ScalazShims {

  implicit def ScalazFoldableShim[F[_]: Foldable] =
    new FoldableShim[F] {
      def toList[A](fa: F[A]) = fa.toList
    }

}

trait ScalazInstances {

  implicit val ParserMonad: Monad[Parser] =
    new Monad[Parser] {
      def point[A](a: => A): Parser[A] = ok(a)
      def bind[A,B](ma: Parser[A])(f: A => Parser[B]) = ma flatMap f
      override def map[A,B](ma: Parser[A])(f: A => B) = ma map f
    }

  implicit val ParserPlus: Plus[Parser] =
    new Plus[Parser] {
      def plus[A](a: Parser[A], b: => Parser[A]): Parser[A] = a | b
    }

  implicit def ParserMonoid[A]: Monoid[Parser[A]] =
    new Monoid[Parser[A]] {
      def append(s1: Parser[A], s2: => Parser[A]): Parser[A] = s1 | s2
      val zero: Parser[A] = err("zero")
    }

  implicit val ParseResultFunctor: Functor[ParseResult] =
    new Functor[ParseResult] {
      def map[A,B](ma: ParseResult[A])(f: A => B) =
        ma map f
    }

}
