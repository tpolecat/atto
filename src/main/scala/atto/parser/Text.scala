package atto
package parser

import language.higherKinds
import scalaz.Monad
import scalaz.std.string._
import scalaz.syntax.std.boolean._
import scalaz.std.list._
import scalaz.syntax.std.option._
import scalaz.syntax.foldable._
import atto.syntax.parser._

/** Text parsers. */
trait Text {
  import combinator._
  import character._

  def stringOf(p: Parser[Char]): Parser[String] =
    many(p).map(_.mkString) as "stringOf(" + p + ")"

  def stringOf1(p: Parser[Char]): Parser[String] =
    many1(p).map(_.mkString) as "stringOf1(" + p + ")"

  /** Parser that skips a `Char` if it satisfies predicate `p`. */
  def skip(s: String, p: Char => Boolean, what: => String = "skip(...)"): Parser[Unit] = 
    ensure(1) ~> get flatMap (s => {
      if (p(s.charAt(0))) put(s.substring(1))
      else err(what)
    }) asOpaque what

  /** Parser that returns a `String` of length `n` if it satisfies predicate `p`. */
  def takeWith(n: Int, p: String => Boolean, what: => String = "takeWith(...)"): Parser[String] =
    ensure(n) ~> get flatMap (s => {
      val w = s.substring(0,n)
      if (p(w)) put(s.substring(n)) ~> ok(w)
      else err(what)
    }) asOpaque what

  /** Parser that returns the next `n` characters as a `String`. */
  def take(n: Int): Parser[String] = 
    takeWith(n, _ => true, "take(" + n + ")")

  /** Parser that matches and returns only `s`. */
  def string(s: String): Parser[String] = 
    takeWith(s.length, _ == s, "\"" + s + "\"")

  def stringTransform(f: String => String, s: String, what: => String = "stringTransform(...)"): Parser[String] = 
    takeWith(s.length, f(_) == f(s), what)

  /** Parser that matches a char optionally, otherwise fails. */
  def optElem[A](p: Char => Option[A], what: => String = "optElem(...)"): Parser[A] = 
    ensure(1) ~> get flatMap { s => 
      p(s.head).cata(a => put(s.tail) ~> ok(a), err(what))
    } asOpaque what

  ////// FROM RUNAR
 
  def takeWhile(p: Char => Boolean): Parser[String] = {
    def go(acc: List[String]): Parser[List[String]] = for {
      x <- get
      (h, t) = x span p
      _ <- put(t)
      r <- if (t.isEmpty) for {
        input <- wantInput
        r <- if (input) go(h :: acc)
             else ok(h :: acc)
      } yield r else ok(h :: acc)
    } yield r
    go(Nil).map(_.reverse.concatenate)
  }

  lazy val takeRest: Parser[List[String]] = {
    def go(acc: List[String]): Parser[List[String]] = for {
      input <- wantInput
      r <- if (input) for {
        s <- get
        _ <- put("")
        r <- go(s :: acc)
      } yield r else ok(acc.reverse)
    } yield r
    go(Nil)
  }

  lazy val takeText: Parser[String] =
    takeRest map (_.concatenate)

  def takeWhile1(p: Char => Boolean): Parser[String] = for {
    _ <- get map (_.isEmpty) flatMap (_.whenM(demandInput))
    s <- get
    (h, t) = s span p
    _ <- h.isEmpty.whenM(err("takeWhile1"):Parser[Unit])
    _ <- put(t)
    r <- if (t.isEmpty) takeWhile(p).map(h + _) else ok(h)
  } yield r


  sealed trait Scan[+S]
  case class Continue[S](s: S) extends Scan[S]
  case class Finished(n: Int, s: String) extends Scan[Nothing]

  def scan[S](s: S)(p: (S, Char) => Option[S]): Parser[String] = {
    def scanner(s: S, n: Int, t: String): Scan[S] = {
      if (t.isEmpty) Continue(s)
      else p(s, t.head) match {
        case Some(s) => scanner(s, n + 1, t.tail)
        case None => Finished(n, t)
      }
    }
    def go(acc: List[String], s: S): Parser[List[String]] = for {
      input <- get
      r <- scanner(s, 0, input) match {
        case Continue(sp) => for {
          _ <- put("")
          more <- wantInput
          r <- if (more) go(input :: acc, sp) else ok(input :: acc)
        } yield r
        case Finished(n, t) => put(t).flatMap(_ => ok(input.take(n) :: acc))
      }
    } yield r
    for {
      chunks <- go(Nil, s)
      r <- chunks match {
        case List(x) => ok(x)
        case xs => ok(xs.reverse.concatenate)
      }
    } yield r
  }

}





