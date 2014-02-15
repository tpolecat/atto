package atto
package parser

import language._
import scalaz.Digit
import scalaz.Digit._
import scalaz.std.list._
import atto.syntax.parser._

/** Parsers for various kinds of characters. */
trait Character {
  import combinator._
  import text._

  /** Parser that returns a `Char` if it satisfies predicate `p`. */
  def elem(p: Char => Boolean, what: => String = "elem(...)"): Parser[Char] = 
    ensure(1) ~> get flatMap (s => {
      val c = s.charAt(0)
      if (p(c)) put(s.substring(1)) ~> ok(c)
      else err(what)
    }) asOpaque what

  /** Equivalent to `elem(p)` but without optional label arg. */
  def satisfy(p: Char => Boolean): Parser[Char] = 
    elem(p, "satisfy(...)")

  /** Parser that matches and returns only `c`. */
  def char(c: Char): Parser[Char] = 
    elem(_ == c, s"'$c'")

  /** Parser that matches any character. */
  lazy val anyChar: Parser[Char] = 
    satisfy(_ => true)

  /** Parser that matches any character other than `c`. */
  def notChar(c: Char): Parser[Char] = 
    satisfy(_ != c) as ("not '" + c + "'")

  def digit: Parser[Char] =
    elem(_.isDigit, "digit")

  def letter: Parser[Char] =
    elem(_.isLetter, "letter")

  def letterOrDigit: Parser[Char] =
    elem(_.isLetterOrDigit, "letterOrDigit")

  def lower: Parser[Char] =
    elem(_.isLower, "lower")

  def spaceChar: Parser[Char] =
    elem(_.isSpaceChar, "spaceChar")

  def upper: Parser[Char] =
    elem(_.isUpper, "upper")

  type CharRange = collection.immutable.NumericRange[Char]

  def charRange(rs: CharRange*) =
    elem(c => rs.exists(_.contains(c)))

}