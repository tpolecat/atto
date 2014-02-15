package atto
package parser

import scalaz.syntax.applicative._
import scalaz.std.list._
import atto.syntax.parser._

/** Parsers for built-in numeric types. */
trait Numeric {
  import combinator._
  import text._
  import character._

  ////// Integral

  /** Parser for an arbitrary-precision integer. */
  val bigInt: Parser[BigInt] =
    (signum |@| stringOf1(digit))(_ * BigInt.apply(_)) asOpaque "bigInt"

  /** Parser for a Long (range-checked). */
  val long: Parser[Long] =
    narrow(bigInt)(_.isValidLong, _.toLong, "long")

  /** Parser for an Int (range-checked). */
  val int: Parser[Int] =
    narrow(bigInt)(_.isValidInt, _.toInt, "int")
  
  /** Parser for a Short (range-checked). */
  val short: Parser[Short] =
    narrow(bigInt)(_.isValidShort, _.toShort, "short")

  /** Parser for a Byte (range-checked). */
  val byte: Parser[Byte] =
    narrow(bigInt)(_.isValidByte, _.toByte, "byte")

  ////// Real

  /** Parser for an arbitrary-precision decimal. */
  val bigDecimal: Parser[BigDecimal] = 
    (signum |@| stringOf1(digit) |@| opt(char('.') ~> stringOf(digit))) {
      case (s, a, Some(b)) => s * BigDecimal(s"$a.$b")
      case (s, a, None)    => s * BigDecimal(a)
    } as "bigDecimal"

  /** Parser for a Double (unchecked narrowing). */
  val double: Parser[Double] =
    bigDecimal.map(_.toDouble) as "double"

  /** Parser for a Float (unchecked narrowing). */
  val float: Parser[Float] = 
    bigDecimal.map(_.toFloat) as "float"

  ////// Helpers

  /** Parser for a leading `+` or `-` as a signum, defaulting to `1` */
  def signum: Parser[Int] =
    char('+').map(_ => 1) | char('-').map(_ => -1) | ok(1)

  private def narrow[A,B](p: Parser[A])(f: A => Boolean, g: A => B, as: String): Parser[B] =
    p flatMap { a => 
      if (f(a)) ok(g(a)) else err("too large, too small, or too precise: " + a)
    } as as

}