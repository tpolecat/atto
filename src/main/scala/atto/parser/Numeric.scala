package atto
package parser

import scalaz.Digit
import scalaz.Digit._
import scalaz.std.list._
import atto.syntax.parser._

trait Numeric {
  import combinator._
  import text._
  
  /** Parser for a decimal digit. */
  val digit: Parser[Digit] = 
    optElem(Digit.digitFromChar, "digit")

  /** Parser for a decimal number. */
  val long: Parser[Long] =
    many1(digit).map(Digit.longDigits(_)) as "long"

  /** Parser for a decimal number. */
  val int: Parser[Int] =
    long.map(_.toInt) as "int"
  
}