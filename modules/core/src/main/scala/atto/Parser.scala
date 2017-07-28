package atto

import cats.{ Eval, Monad, SemigroupK, Monoid }
import java.lang.{ String, SuppressWarnings }
import scala.{ Array, Boolean, List, Unit, Int, Either, Left, Right }
import scala.Predef.augmentString

// Operators not needed for use in `for` comprehensions are provided via added syntax.
trait Parser[A] { m =>
  import Parser._

  def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R]

  // TODO: get rid of this
  def infix(s: String): String =
    "(" + m.toString + ") " + s

  def flatMap[B](f: A => Parser[B]): Parser[B] =
    new Parser[B] {
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        Eval.defer(m(st0,kf,(s:State, a:A) => f(a)(s,kf,ks)))
      override def toString = m infix "flatMap ..."
    }

  def map[B](f: A => B): Parser[B] =
    new Parser[B] {
      override def toString = m infix "map ..."
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        Eval.defer(m(st0,kf,(s:State, a:A) => Eval.defer(ks(s,f(a)))))
    }

  def filter(p: A => Boolean): Parser[A] =
    parser.combinator.filter(this)(p)

  def void: Parser[Unit] =
    this.map(_ => ())

  override def toString = "Parser(...)"

}

object Parser extends ParserFunctions with ParserInstances {

  type Pos = Int

  final case class State private (input: String, pos: Pos, complete: Boolean) {
    def completed: State = copy(complete = true)
  }

  object State {
    @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
    def apply(s: String, done: Boolean): State =
      new State(s, 0, done)
  }

  object Internal {
    sealed abstract class Result[T] {
      def translate: ParseResult[T]
    }
    final case class Fail[T](input: State, stack: List[String], message: String) extends Result[T] {
      def translate = ParseResult.Fail(input.input, stack, message)
      def push(s: String): Fail[T] = Fail(input, stack = s :: stack, message)
    }
    final case class Partial[T](k: String => Eval[Result[T]]) extends Result[T] {
      def translate = ParseResult.Partial(a => k(a).value.translate)
    }
    final case class Done[T](input: State, result: T) extends Result[T] {
      def translate = ParseResult.Done(input.input, result)
    }
  }

  import Internal._

  type TResult[R] = Eval[Result[R]]
  type Failure[R] = (State,List[String],String) => TResult[R]
  type Success[-A, R] = (State, A) => TResult[R]

}

trait ParserFunctions {
  import Parser._
  import Parser.Internal._

  /**
   * Run a parser
   */
  def parse[A](m: Parser[A], b: String): ParseResult[A] = {
    def kf(a:State, b: List[String], c: String) = Eval.now[Result[A]](Fail(a.copy(input = a.input.drop(a.pos)), b, c))
    def ks(a:State, b: A) = Eval.now[Result[A]](Done(a.copy(input = a.input.drop(a.pos)), b))
    m(State.apply(b, false), kf, ks).value.translate
  }

  /**
   * Run a parser that cannot be resupplied via a 'Partial' result.
   *
   * This function does not force a parser to consume all of its input.
   * Instead, any residual input will be discarded.
   */
  def parseOnly[A](m: Parser[A], b: String): ParseResult[A] = {
    def kf(a:State, b: List[String], c: String) = Eval.now[Result[A]](Fail(a.copy(input = a.input.drop(a.pos)), b, c))
    def ks(a:State, b: A) = Eval.now[Result[A]](Done(a.copy(input = a.input.drop(a.pos)), b))
    m(State.apply(b, true), kf, ks).value.translate
  }

}

trait ParserInstances {
  import Atto._

  implicit val ParserMonad: Monad[Parser] =
    new Monad[Parser] {
      def pure[A](a: A): Parser[A] = ok(a)
      def flatMap[A,B](ma: Parser[A])(f: A => Parser[B]) = ma flatMap f
      override def map[A,B](ma: Parser[A])(f: A => B) = ma map f
      @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
      def tailRecM[A, B](a: A)(f: A => Parser[Either[A, B]]): Parser[B] =
        f(a).flatMap {
          case Left(a)  => tailRecM(a)(f)
          case Right(b) => pure(b)
        }
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

}
