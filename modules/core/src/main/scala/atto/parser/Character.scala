package atto
package parser

import cats.implicits._
import java.lang.String
import atto.syntax.parser._
import scala.{ Char, Boolean, Option, Unit }
import scala.Predef.{ charWrapper, intWrapper, augmentString }

/** Parsers for various kinds of characters. */
trait Character {
  import combinator._

  /** Parser that returns a `Char` if it satisfies predicate `p`. */
  def elem(p: Char => Boolean, what: => String = "elem(...)"): Parser[Char] =
    ensure(1) flatMap (s => {
      val c = s.charAt(0)
      if (p(c)) advance(1) ~> ok(c)
      else err[Char](what)
    }) namedOpaque what

  /** Equivalent to `elem(p)` but without optional label arg. */
  def satisfy(p: Char => Boolean): Parser[Char] =
    elem(p, "satisfy(...)")

  /** Character is in the given String */
  def oneOf(s: String): Parser[Char] = satisfy(c => s.contains(c))

  /** Character is not in the given String */
  def noneOf(s: String): Parser[Char] = satisfy(c => !s.contains(c))

  /** Parser that matches and returns only `c`. */
  def char(c: Char): Parser[Char] =
    elem(_ === c, "'" + c.toString + "'")

  /** Parser that matches any character. */
  lazy val anyChar: Parser[Char] =
    satisfy(_ => true)

  /** Parser that matches any character other than `c`. */
  def notChar(c: Char): Parser[Char] =
    satisfy(_ =!= c) named ("not '" + c.toString + "'")

  /** Decimal digit, 0-9. */
  def decimalDigit: Parser[Char] =
    charRange('0' to '9') named "decimalDigit"

  /** Hex digit, 0-9, A-F, a-f */
  def hexDigit: Parser[Char] =
    charRange('0' to '9', 'a' to 'f', 'A' to 'F')

  /** Binary digit, 0 or 1 */
  def binaryDigit: Parser[Char] =
    elem {
      case '0' | '1' => true
      case _ => false
    }

  /** Octal digit, 0-7 */
  def octalDigit: Parser[Char] =
    charRange('0' to '7')

  /** The following parsers have been derived from Character.java.
   *  Note that they also handle non-Western unicode characters.
   *  See Java documentation for details. */

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

  def whitespace: Parser[Char] =
    elem(c => c.isWhitespace, "whitespace")

  type CharRange = scala.collection.immutable.NumericRange[Char]

  def charRange(rs: CharRange*): Parser[Char] =
    elem(c => rs.exists(_.contains(c)))

  /** `elem` + `map` in a single operation. */
  def optElem[A](p: Char => Option[A]): Parser[A] =
    ensure(1) flatMap { s =>
      p(s.head).fold[Parser[A]](err("optElem(...)"))(a => advance(1) ~> ok(a))
    } named "optElem(...)"

  /** Parser that skips a `Char` if it satisfies predicate `p`. */
  def skip(p: Char => Boolean): Parser[Unit] =
    elem(p).void named "skip(...)"

  /** Whitespace that is not a line break */
  def horizontalWhitespace: Parser[Char] =
    oneOf(s" \t\u00A0\u1680\u180e${(0x2000 to 0x200a).map(_.toChar).mkString}\u202f\u205f\u3000").named("horizontalWhitespace")
}
