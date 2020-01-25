package atto
package parser

import cats.{ Eval, Foldable }
import cats.data.NonEmptyList
import cats.implicits._

import java.lang.{ String, SuppressWarnings }
import scala.{ Array, Nil, Int, Unit, List, Boolean, Either, Left, Right, StringContext, PartialFunction, Option, Some }
import scala.Predef.augmentString

import atto.syntax.all._

// These guys need access to the implementation
trait Combinator0 {

  import Parser._
  import Parser.Internal._
  import atto.syntax.all._

  /** Parser that consumes no data and produces the specified value. */
  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  def ok[A](a: A): Parser[A] =
    new Parser[A] {
      override def toString = "ok(" + a.toString + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        Eval.defer(ks(st0,a))
    }

  /** Parser that consumes no data and fails with the specified error message. */
  def err[A](what: String): Parser[A] =
    new Parser[A] {
      override def toString = "err(" + what + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        Eval.defer(kf(st0,Nil, what))
    }

  /** Construct the given parser lazily; useful when defining recursive parsers. */
  def delay[A](p: => Parser[A]): Parser[A] = {
    lazy val a = p
    new Parser[A] {
      override def toString = a.toString
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        a.apply(st0, kf, ks)
    }
  }

  //////

  def advance(n: Int): Parser[Unit] =
    new Parser[Unit] {
      override def toString = "advance(" + n.toString + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit, R]): TResult[R] =
        ks(st0.copy(pos = st0.pos + n),())
    }

  private def prompt[R](st0: State, kf: State => TResult[R], ks: State => TResult[R]): Result[R] =
    Partial[R](s =>
      if (s.isEmpty) Eval.defer(kf(st0 copy (complete = true)))
      else Eval.defer(ks(st0 copy (input = st0.input + s, complete = false)))
    )

  def demandInput: Parser[Unit] =
    new Parser[Unit] {
      override def toString = "demandInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] =
        if (st0.complete)
          Eval.defer(kf(st0,List(),"not enough bytes"))
        else
          Eval.now(prompt(st0, st => kf(st,List(),"not enough bytes"), a => ks(a,())))
    }

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def ensureSuspended(n: Int): Parser[String] =
    new Parser[String] {
      override def toString = "ensureSuspended(" + n.toString + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): TResult[R] =
        if (st0.input.length >= st0.pos + n)
          Eval.defer(ks(st0,st0.input.substring(st0.pos, st0.pos + n)))
        else
          Eval.defer((demandInput ~> ensureSuspended(n))(st0,kf,ks))
    }

  def ensure(n: Int): Parser[String] =
    new Parser[String] {
      override def toString = "ensure(" + n.toString + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): TResult[R] =
        if (st0.input.length >= st0.pos + n)
          Eval.defer(ks(st0,st0.input.substring(st0.pos, st0.pos + n)))
        else
          Eval.defer(ensureSuspended(n)(st0,kf,ks))
    }

  val wantInput: Parser[Boolean] =
    new Parser[Boolean] {
      override def toString = "wantInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Boolean,R]): TResult[R] =
        if (st0.input.length >= st0.pos + 1)   Eval.defer(ks(st0,true))
        else if (st0.complete) Eval.defer(ks(st0,false))
        else Eval.now(prompt(st0, a => ks(a,false), a => ks(a,true)))
    }

  //////

  /** Parser that produces the remaining input (but does not consume it). */
  val get: Parser[String] =
    new Parser[String] {
      override def toString = "get"
      def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): TResult[R] =
        Eval.defer(ks(st0,st0.input.drop(st0.pos)))
    }

  /* Parser that produces the current offset in the input. */
  val pos: Parser[Int] =
    new Parser[Int] {
      override def toString = "pos"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Int,R]): TResult[R] =
        Eval.defer(ks(st0,st0.pos))
    }

  def endOfChunk: Parser[Boolean] =
    new Parser[Boolean] {
      override def toString = "endOfChunk"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Boolean,R]): TResult[R] =
        Eval.defer(ks(st0,st0.pos === st0.input.length))
    }

  //////

  /**
   * Attoparsec `try`, for compatibility reasons. This is actually a no-op
   * since atto parsers always rewind in case of failure.
   */
  def attempt[T](p: Parser[T]): Parser[T] = p

  /** Parser that matches end of input. */
  val endOfInput: Parser[Unit] =
    new Parser[Unit] {
      override def toString = "endOfInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] =
        Eval.defer(if (st0.pos >= st0.input.length) {
          if (st0.complete)
            ks(st0,())
          else
            demandInput(
              st0,
              (st1: State, _: List[String], _: String) => ks(st1,()),
              (st1: State, _: Unit) => kf(st1, Nil, "endOfInput")
            )
        } else kf(st0,Nil,"endOfInput"))
    }


  def discardLeft[A,B](m: Parser[A], b: => Parser[B]): Parser[B] = {
    lazy val n = b
    new Parser[B] {
      override def toString = m infix ("~> " + n.toString)
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        Eval.defer(m(st0,kf,(s:State, _: A) => n(s, kf, ks)))
    }
  }

  def discardRight[A, B](m: Parser[A], b: => Parser[B]): Parser[A] = {
    lazy val n = b
    new Parser[A] {
      override def toString = m infix ("<~ " + n.toString)
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        Eval.defer(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, _: B) => ks(st2, a))))
    }
  }

  def andThen[A, B](m: Parser[A], b: => Parser[B]): Parser[(A,B)] = {
    lazy val n = b
    new Parser[(A,B)] {
      override def toString = m infix ("~ " + n.toString)
      def apply[R](st0: State, kf: Failure[R], ks: Success[(A,B),R]): TResult[R] =
        Eval.defer(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, b: B) => ks(st2, (a, b)))))
    }
  }

  def orElse[A, B >: A](m: Parser[A], b: => Parser[B]): Parser[B] = {
    lazy val n = b
    new Parser[B] {
      override def toString = m infix ("| ...")
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        Eval.defer(m(st0, (st1: State, _: List[String], _: String) => n(st1.copy(pos = st0.pos), kf, ks), ks))
    }
  }

  def either[A, B](m: Parser[A], b: => Parser[B]): Parser[Either[A,B]] = {
    lazy val n = b
    new Parser[Either[A,B]] {
      override def toString = m infix ("|| " + n.toString)
      def apply[R](st0: State, kf: Failure[R], ks: Success[Either[A, B], R]): TResult[R] =
        Eval.defer(m(
          st0,
          (st1: State, _: List[String], _: String) => n (st1.copy(pos = st0.pos), kf, (st1: State, b: B) => ks(st1, Right(b))),
          (st1: State, a: A) => ks(st1, Left(a))
        ))
    }
  }

  def modifyName[A](m: Parser[A], f: String => String): Parser[A] =
    named(m, f(m.toString))

  def named[A](m: Parser[A], s: => String): Parser[A] =
    new Parser[A] {
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        Eval.defer(m(st0, (st1: State, stack: List[String], msg: String) => kf(st1, s :: stack, msg), ks))
    }

  def namedOpaque[A](m: Parser[A], s: => String): Parser[A] =
    new Parser[A] {
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        Eval.defer(m(st0, (st1: State, _: List[String], _: String) => kf(st1, Nil, "Failure reading:" + s), ks))
    }

}

// These don't need access to the implementation
trait Combinator extends Combinator0 {

  import scala.Predef.intWrapper

  def collect[A, B](m: Parser[A], f: PartialFunction[A,B]): Parser[B] =
    m.filter(f isDefinedAt _).map(f)

  def cons[A, B >: A](m: Parser[A], n: => Parser[List[B]]): Parser[NonEmptyList[B]] =
    m flatMap (x => n map (xs => NonEmptyList(x, xs)))

  /** Parser that matches `p` only if there is no remaining input */
  def phrase[A](p: Parser[A]): Parser[A] =
    p <~ endOfInput named ("phrase(" + p.toString + ")")

  // TODO: return a parser of a reducer of A
  /** Parser that matches zero or more `p`. */
  def many[A](p: => Parser[A]): Parser[List[A]] = {
    lazy val many_p : Parser[List[A]] = cons(p, many_p).map(_.toList) | ok(Nil)
    many_p named ("many(" + p.toString + ")")
  }

  /** Parser that matches one or more `p`. */
  def many1[A](p: => Parser[A]): Parser[NonEmptyList[A]] =
    cons(p, many(p))

  def manyN[A](n: Int, a: Parser[A]): Parser[List[A]] =
    (1 to n).foldRight(ok(List[A]()))((_, p) => cons(a, p).map(_.toList)) named "ManyN(" + n.toString + ", " + a.toString + ")"

  def manyUntil[A](p: Parser[A], q: Parser[_]): Parser[List[A]] = {
    lazy val scan: Parser[List[A]] = (q ~> ok(Nil)) | cons(p, scan).map(_.toList)
    scan named ("manyUntil(" + p.toString + "," + q.toString + ")")
  }

  def skipMany(p: Parser[_]): Parser[Unit] =
    many(p).void named s"skipMany($p)"

  def skipMany1(p: Parser[_]): Parser[Unit] =
    many1(p).void named s"skipMany1($p)"

  def skipManyN(n: Int, p: Parser[_]): Parser[Unit] =
    manyN(n, p).void named s"skipManyN($n, $p)"

  def sepBy[A](p: Parser[A], s: Parser[_]): Parser[List[A]] =
    cons(p, ((s ~> sepBy1(p,s)).map(_.toList) | ok(List.empty[A]))).map(_.toList) | ok(List.empty[A]) named ("sepBy(" + p.toString + "," + s.toString + ")")

  def sepBy1[A](p: Parser[A], s: Parser[_]): Parser[NonEmptyList[A]] = {
    lazy val scan: Parser[NonEmptyList[A]] = cons(p, s ~> scan.map(_.toList) | ok(Nil))
    scan named ("sepBy1(" + p.toString + "," + s.toString + ")")
  }

  // Delimited pair
  def pairBy[A,B](a: Parser[A], delim: Parser[_], b: Parser[B]): Parser[(A,B)] =
    (a <~ delim) ~ b

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def choice[A](xs: Parser[A]*) : Parser[A] =
    xs.foldRight[Parser[A]](err("choice: no match"))(_ | _) named s"choice(${xs.mkString(", ")})"

  @SuppressWarnings(Array("org.wartremover.warts.Overloading"))
  def choice[F[_]: Foldable, A](fpa: F[Parser[A]]): Parser[A] =
    choice(fpa.toList: _*)

  def opt[A](m: Parser[A]): Parser[Option[A]] =
    (attempt(m).map[Option[A]](Some(_)) | ok(Option.empty[A])) named s"opt($m)"

  def filter[A](m: Parser[A])(p: A => Boolean): Parser[A] =
    m.flatMap { a =>
      if (p(a)) ok(a) else err[A]("filter")
    } named "filter(...)"

  def count[A](n: Int, p: Parser[A]): Parser[List[A]] =
    (1 to n).foldRight(ok(List[A]()))((_, a) => cons(p, a).map(_.toList))

}
