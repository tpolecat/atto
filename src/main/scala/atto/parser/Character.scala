package atto
package parser

import language._
import scalaz.Digit
import scalaz.Digit._
import scalaz.std.list._
import atto.syntax.parser._

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

  /** Parser that matches and returns only `c`. */
  def char(c: Char): Parser[Char] = 
    elem(_==c, "'" + c.toString + "'")

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

}