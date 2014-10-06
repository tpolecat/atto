package atto
package parser

import atto.syntax.parser._
import java.lang.{ String, Integer }
import scala.{ Char, List, Int, StringContext, Boolean, Nil, Nothing, Unit, Option, Some, None }
import scala.Predef.{ augmentString, charWrapper }
import scala.language.higherKinds
import scalaz.Monad
import scalaz.std.string._
import scalaz.syntax.std.boolean._
import scalaz.std.list._
import scalaz.syntax.std.option._
import scalaz.syntax.foldable._

/** Text parsers. */
trait Text {
  import combinator._
  import character._

  /** Parser that returns a string of characters matched by `p`. */
  def stringOf(p: Parser[Char]): Parser[String] =
    many(p).map(cs => new String(cs.toArray)) named "stringOf(" + p + ")"

  /** Parser that returns a non-empty string of characters matched by `p`. */
  def stringOf1(p: Parser[Char]): Parser[String] =
    many1(p).map(cs => new String(cs.toArray)) named "stringOf1(" + p + ")"

  def takeWith(n: Int, p: String => Boolean, what: => String = "takeWith(...)"): Parser[String] =
    ensure(n) flatMap { s =>
      if (p(s)) advance(n) ~> ok(s)
      else err(what)
    } namedOpaque what

  /** Parser that returns the next `n` characters as a `String`. */
  def take(n: Int): Parser[String] =
    takeWith(n, _ => true, "take(" + n + ")")

  /** Parser that matches and returns only `s`. */
  def string(s: String): Parser[String] =
    takeWith(s.length, _ == s, "string(\"" + s + "\")")

  /** Like `string` but case-insensitive `s`. */
  def stringCI(s: String): Parser[String] =
    takeWith(s.length, _ equalsIgnoreCase s, "stringCI(\"" + s + "\")")

  /**
   * Parser that returns a string of characters passing the supplied predicate. Equivalent to but
   * more efficient than `stringOf(elem(p))`.
   */
  def takeWhile(p: Char => Boolean): Parser[String] = {
    def go(acc: List[String]): Parser[List[String]] = for {
      x <- get map (_.takeWhile(p))
      _ <- advance(x.length)
      eoc <- endOfChunk
      r <- if (eoc) for {
        input <- wantInput
        r <- if (input) go(x :: acc)
             else ok(x :: acc)
      } yield r else ok(x :: acc)
    } yield r
    go(Nil).map(_.reverse.concatenate)
  }

  private def when(m: Parser[Unit], b: Boolean) = if (b) m else ok(())

 /**
   * Parser that returns a non-empty string of characters passing the supplied predicate. Equivalent
   * to but more efficient than `stringOf1(elem(p))`.
   */
  def takeWhile1(p: Char => Boolean): Parser[String] = for {
    _ <- endOfChunk.flatMap(when(demandInput, _))
    x <- get map (_.takeWhile(p))
    len = x.length
    r <- if (len == 0) err("takeWhile1")
      else for {
        _ <- advance(len)
        eoc <- endOfChunk
        r <- if (eoc) takeWhile(p) map (x + _)
          else ok(x)
      } yield r
  } yield r

  /** Parser that consumes and returns all remaining chunks of input. */
  lazy val takeRest: Parser[List[String]] = {
    def go(acc: List[String]): Parser[List[String]] = for {
      input <- wantInput
      r <- if (input) for {
        s <- get
        _ <- advance(s.length)
        r <- go(s :: acc)
      } yield r else ok(acc.reverse)
    } yield r
    go(Nil)
  }

  /** Parser that consumes and returns all remaining input. */
  lazy val takeText: Parser[String] =
    takeRest map (_.concatenate)

  /**
   * Stateful scanning parser returning a string of all visited chars. Many combinators can be
   * written in terms of `scan`; for instance, `take(n)` could be written  as
   * `scan(n)((m, _) => m > 0 option m - 1)`.
   */
  def scan[S](s: S)(p: (S, Char) => Option[S]): Parser[String] = {

    sealed trait Scan[+S]
    case class Continue[S](s: S) extends Scan[S]
    case class Finished(n: Int, s: String) extends Scan[Nothing]

    def scanner(s: S, n: Int, t: String): Scan[S] = {
      if (t.isEmpty) Continue(s)
      else p(s, t.head) match {
        case Some(s) => scanner(s, n + 1, t.tail)
        case None    => Finished(n, t)
      }
    }

    def go(acc: List[String], s: S): Parser[List[String]] = for {
      input <- get
      r <- scanner(s, 0, input) match {
        case Continue(sp) => for {
          _    <- advance(input.length)
          eoc <- endOfChunk
          r <- if (eoc) for {
            more <- wantInput
            r <- if (more) go(input :: acc, sp) else ok(input :: acc)
          } yield r
            else Parser.monoid.zero
        } yield r
        case Finished(n, t) => advance(input.length - t.length).flatMap(_ => ok(input.take(n) :: acc))
      }
    } yield r

    go(Nil, s).map(_.reverse.concatenate)

  }

  /** Quoted strings with control and unicode escapes, Java/JSON style. **/
  val stringLiteral: Parser[String] = {

    // Unescaped characters
    val nesc: Parser[Char] =
      elem(c => c != '\\' && c != '"' && !c.isControl)

    // Escaped characters
    val esc: Parser[Char] =
      string("\\\"") >| '"'  |
      string("\\\\") >| '\\' |
      string("\\/")  >| '/'  |
      string("\\b")  >| '\b' |
      string("\\f")  >| '\f' |
      string("\\n")  >| '\n' |
      string("\\r")  >| '\r' |
      string("\\t")  >| '\t'

    // Unicode escaped characters
    val unicode: Parser[Char] =
      string("\\u") ~> count(4, hexDigit).map(ds => Integer.parseInt(new String(ds.toArray), 16).toChar)

    // Quoted strings
    char('"') ~> many(nesc | esc | unicode ).map(cs => new String(cs.toArray)) <~ char('"')

  } named "stringLiteral"

}





