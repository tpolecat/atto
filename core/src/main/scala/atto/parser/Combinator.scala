package atto
package parser

import java.lang.String
import scala.{ Boolean, Nothing, Unit, Int, Nil, List, PartialFunction, StringContext, Option }
import scala.language.higherKinds
import scalaz.Scalaz.{ some, none }
import scalaz.syntax.monad._
import atto.syntax.all._

// These guys need access to the implementation
trait Combinator0 {
  
  import scalaz.Free.Trampoline
  import scalaz.Trampoline
  import scalaz.{\/, -\/, \/-}
  import Trampoline._
  import Parser._
  import Parser.Internal._
  import atto.syntax.all._

  /** Parser that consumes no data and produces the specified value. */
  def ok[A](a: A): Parser[A] = 
    new Parser[A] { 
      override def toString = "ok(" + a + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] = 
        suspend(ks(st0,a))
    }

  /** Parser that consumes no data and fails with the specified error message. */
  def err(what: String): Parser[Nothing] = 
    new Parser[Nothing] {
      override def toString = "err(" + what + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Nothing,R]): TResult[R] = 
        suspend(kf(st0,Nil, what))
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

  private def prompt[R](st0: State, kf: State => TResult[R], ks: State => TResult[R]): Result[R] = 
    Partial[R](s => 
      if (s == "") suspend(kf(st0 copy (complete = true)))
      else suspend(ks(st0 + s))
    )

  def demandInput: Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "demandInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] = 
        if (st0.complete)
          suspend(kf(st0,List("demandInput"),"not enough bytes"))
        else
          done(prompt(st0, st => kf(st,List("demandInput"),"not enough bytes"), a => ks(a,())))
    }

  def ensure(n: Int): Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "ensure(" + n + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] = 
        if (st0.input.length >= n)
          suspend(ks(st0,()))
        else
          suspend((demandInput ~> ensure(n))(st0,kf,ks))
    }

  val wantInput: Parser[Boolean] = 
    new Parser[Boolean] {
      override def toString = "wantInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Boolean,R]): TResult[R] = 
        if (st0.input != "")   suspend(ks(st0,true))
        else if (st0.complete) suspend(ks(st0,false))
        else done(prompt(st0, a => ks(a,false), a => ks(a,true)))
    }

  //////

  /** Parser that produces the remaining input (but does not consume it). */
  val get: Parser[String] = 
    new Parser[String] {
      override def toString = "get"
      def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): TResult[R] = 
        suspend(ks(st0,st0.input))
    }

  /** Parser that replaces the remaining input and produces (). */
  def put(s: String): Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "put(" + s + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] = 
        suspend(ks(st0 copy (input = s), ()))
    }

  //////

  // attoparsec try
  def attempt[T](p: Parser[T]): Parser[T] = 
    new Parser[T] { 
      override def toString = "attempt(" + p + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[T,R]): TResult[R] = 
        suspend(p(st0.noAdds, (st1: State, stack: List[String], msg: String) => kf(st0 + st1, stack, msg), ks))
    }
  
  /** Parser that matches end of input. */
  val endOfInput: Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "endOfInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): TResult[R] = 
        suspend(if (st0.input == "") {
          if (st0.complete)
            ks(st0,())
          else 
            demandInput(
              st0,
              (st1: State, stack: List[String], msg: String) => ks(st0 + st1,()), 
              (st1: State, u: Unit) => kf(st0 + st1, Nil, "endOfInput")
            )
        } else kf(st0,Nil,"endOfInput"))
    }


  def discardLeft[A,B](m: Parser[A], n: Parser[B]): Parser[B] = 
    new Parser[B] { 
      override def toString = m infix ("~> " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        suspend(m(st0,kf,(s:State, a: A) => n(s, kf, ks)))
    }

  def discardRight[A, B](m: Parser[A], n: Parser[B]): Parser[A] = 
    new Parser[A] {
      override def toString = m infix ("<~ " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] =
        suspend(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, b: B) => ks(st2, a))))
    }
  
  def andThen[A, B](m: Parser[A], n: Parser[B]): Parser[(A,B)] = 
    new Parser[(A,B)] { 
      override def toString = m infix ("~ " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[(A,B),R]): TResult[R] =
        suspend(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, b: B) => ks(st2, (a, b)))))
    }

  def orElse[A, B >: A](m: Parser[A], n: => Parser[B]): Parser[B] = 
    new Parser[B] {
      override def toString = m infix ("| ...")
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        suspend(m(st0.noAdds, (st1: State, stack: List[String], msg: String) => n(st0 + st1, kf, ks), ks))
    }

  def either[A, B](m: Parser[A], n: => Parser[B]): Parser[\/[A,B]] = 
    new Parser[\/[A,B]] { 
      override def toString = m infix ("|| " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[\/[A,B],R]): TResult[R] = 
        suspend(m(
          st0.noAdds, 
          (st1: State, stack: List[String], msg: String) => n (st0 + st1, kf, (st1: State, b: B) => ks(st1, \/-(b))), 
          (st1: State, a: A) => ks(st1, -\/(a))
        ))
    }

  def named[A](m: Parser[A], s: => String): Parser[A] = 
    new Parser[A] { 
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] = 
        suspend(m(st0, (st1: State, stack: List[String], msg: String) => kf(st1, s :: stack, msg), ks))
    }

  def namedOpaque[A](m: Parser[A], s: => String): Parser[A] = 
    new Parser[A] { 
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R] = 
        suspend(m(st0, (st1: State, stack: List[String], msg: String) => kf(st1, Nil, "Failure reading:" + s), ks))
    }

}

// These don't need access to the implementation
trait Combinator extends Combinator0 {

  import scala.Predef.intWrapper
  import scalaz.Foldable
  import scalaz.syntax.foldable._

  def collect[A, B](m: Parser[A], f: PartialFunction[A,B]): Parser[B] = 
    m.filter(f isDefinedAt _).map(f)

  def cons[A, B >: A](m: Parser[A], n: => Parser[List[B]]): Parser[List[B]] = 
    m flatMap (x => n map (xs => x :: xs))

  /** Parser that matches `p` only if there is no remaining input */
  def phrase[A](p: Parser[A]): Parser[A] = 
    p <~ endOfInput named ("phrase" + p)

  // TODO: return a parser of a reducer of A
  /** Parser that matches zero or more `p`. */
  def many[A](p: => Parser[A]): Parser[List[A]] = {
    lazy val many_p : Parser[List[A]] = cons(p, many_p) | ok(Nil)
    many_p named ("many(" + p + ")")
  }

  /** Parser that matches one or more `p`. */
  def many1[A](p: => Parser[A]): Parser[List[A]] = 
    cons(p, many(p))

  def manyN[A](n: Int, p: Parser[A]): Parser[List[A]] = {
    require(n >= 0, "n must be >= 0")
    ((if (n == 0) ok(Nil) else cons(p, manyN(n - 1, p)))
      named "ManyN(" + n + ", " + p + ")")
  }

  def manyUntil[A](p: Parser[A], q: Parser[_]): Parser[List[A]] = { 
    lazy val scan: Parser[List[A]] = (q ~> ok(Nil)) | cons(p, scan) 
    scan named ("manyUntil(" + p + "," + q + ")")
  }

  def upToN[A](n: Int, a: Parser[A]): Parser[List[A]] = {
    require(n >= 0, "n must be >= 0")
    if (n <= 0) ok(Nil)
    else opt(a).flatMap({
      case None => ok(Nil)
      case Some(r) => ok(r) ~ upToN(n-1, a) map { case (a,rest) => a :: rest }
    })
  } named s"upToN($n, $a)"

  def manyNM[A](n: Int, m: Int, a: Parser[A]): Parser[List[A]] = {
    require(n >= 0, "n must be >= 0")
    require(m >= n, "m must be >= n")
    attempt(manyN(n, a) ~ upToN(m-n, a) map { case (l,r) => l ++ r }) named s"manyNM($n, $m, $a)"
  }

  def skipMany(p: Parser[_]): Parser[Unit] = 
    many(p).void named s"skipMany($p)"
      
  def skipMany1(p: Parser[_]): Parser[Unit] = 
    many1(p).void named s"skipMany1($p)"

  def skipManyN(n: Int, p: Parser[_]): Parser[Unit] = 
    manyN(n, p).void named s"skipManyN($n, $p)"

  def sepBy[A](p: Parser[A], s: Parser[_]): Parser[List[A]] =
    cons(p, ((s ~> sepBy1(p,s)) | ok(Nil))) | ok(Nil) named ("sepBy(" + p + "," + s + ")")

  def sepBy1[A](p: Parser[A], s: Parser[_]): Parser[List[A]] = {
    lazy val scan : Parser[List[A]] = cons(p, s ~> scan | ok(Nil))
    scan named ("sepBy1(" + p + "," + s + ")")
  }

  // Delimited pair
  def pairBy[A,B](a: Parser[A], delim: Parser[_], b: Parser[B]): Parser[(A,B)] =
    (a <~ delim) ~ b

  def choice[A](xs: Parser[A]*) : Parser[A] = 
    xs.foldRight[Parser[A]](err("choice: no match"))(_ | _) named s"choice(${xs.mkString(", ")})"

  def choice[F[_]: Foldable, A](fpa: F[Parser[A]]) : Parser[A] = 
    choice(fpa.toList: _*)

  def opt[A](m: Parser[A]): Parser[Option[A]] = 
    (attempt(m).map(some(_)) | ok(none)) named s"opt($m)"

  def filter[A](m: Parser[A])(p: A => Boolean): Parser[A] =
    m.flatMap { a => 
      if (p(a)) ok(a) else err("filter")
    } named "filter(...)"

  def count[A](n: Int, p: Parser[A]): Parser[List[A]] =
    ((1 to n) :\ ok(List[A]()))((_, a) => cons(p, a))

}
