package atto
package parser

import scalaz.std.list._
import atto.syntax.parser._

trait Numeric {
  import combinator._
  import text._
  import character._
  
  ////// Integral

  /** Parser for an arbitrary-precision integer. */
  val bigInt: Parser[BigInt] =
    stringOf(digit).map(BigInt.apply) as "bigInt"

  /** Parser for a Long. */
  val long: Parser[Long] =
    narrow(bigInt)(_.isValidLong, _.toLong) as "long"

  /** Parser for an Int. */
  val int: Parser[Int] =
    narrow(bigInt)(_.isValidInt, _.toInt) as "int"
  
  /** Parser for a Short. */
  val short: Parser[Short] =
    narrow(bigInt)(_.isValidShort, _.toShort) as "short"

  /** Parser for a Byte. */
  val byte: Parser[Byte] =
    narrow(bigInt)(_.isValidByte, _.toByte) as "byte"

  ////// Real

  /** Parser for an arbitrary-precision decimal. */
  val bigDecimal: Parser[BigDecimal] = 
    (stringOf(digit) ~ opt(char('.') ~> stringOf(digit))) map {
      case (a, Some(b)) => BigDecimal(s"$a.$b")
      case (a, None) => BigDecimal(a)
    } as "bigDecimal"

  /** Parser for a Double. */
  val double: Parser[Double] =
    narrow(bigDecimal)(_.isValidDouble, _.toDouble) as "double"

  /** Parser for a Float. */
  val float: Parser[Float] =
    narrow(bigDecimal)(_.isValidFloat, _.toFloat) as "float"

  ////// Helpers

  private def narrow[A,B](p: Parser[A])(f: A => Boolean, g: A => B): Parser[B] =
    p flatMap { a => 
      if (f(a)) ok(g(a)) else err("out of range: " + a)
    }

}