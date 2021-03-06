package atto
package parser

import atto.syntax.parser._
import java.lang.{ String, SuppressWarnings }
import scala.{ Array, Boolean, Int, BigInt, Long, Short, Byte, BigDecimal, Double, Float, Some, None, StringContext }
import scala.Predef.charWrapper

/** Parsers for built-in numeric types. */
trait Numeric {
  import combinator._
  import text._
  import character._

  ////// Integral

  /** Parser for an arbitrary-precision integer. */
  val bigInt: Parser[BigInt] = {
    for {
      a <- signum
      b <- takeWhile1(_.isDigit)
      n <- try ok(BigInt(a) * BigInt(b))
           catch {
             // scala-js can't parse non-alpha digits so we just fail in that case.
             case _: java.lang.NumberFormatException =>
               err[BigInt]("https://github.com/scala-js/scala-js/issues/2935")
           }
    } yield n
  } namedOpaque "bigInt"

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
  val bigDecimal: Parser[BigDecimal] = {
    for {
      a <- signum
      b <- takeWhile1(_.isDigit)
      c <- opt(char('.') ~> takeWhile(_.isDigit))
      d <- opt(char('E') ~> long)
    } yield {
      (a, b, c, d) match {
        case (s, a, Some(b), Some(e)) => BigDecimal(s) * BigDecimal(s"$a.${b}E$e")
        case (s, a, None, Some(e))    => BigDecimal(s) * BigDecimal(s"${a}E$e")
        case (s, a, Some(b), None)    => BigDecimal(s) * BigDecimal(s"$a.$b")
        case (s, a, None, None)       => BigDecimal(s) * BigDecimal(a)
      }
    }
  } named "bigDecimal"

  /** Parser for a Double (unchecked narrowing). */
  val double: Parser[Double] =
    bigDecimal.map(_.toDouble) named "double"

  /** Parser for a Float (unchecked narrowing). */
  val float: Parser[Float] =
    bigDecimal.map(_.toFloat) named "float"

  ////// Helpers

  /** Parser for a leading `+` or `-` as a signum, defaulting to `1` */
  def signum: Parser[Int] =
    char('+').map(_ => 1) | char('-').map(_ => -1) | ok(1)

  @SuppressWarnings(Array("org.wartremover.warts.ToString"))
  private def narrow[A,B](p: Parser[A])(f: A => Boolean, g: A => B, as: String): Parser[B] =
    p flatMap { a =>
      if (f(a)) ok(g(a)) else err[B]("too large, too small, or too precise: " + a.toString)
    } named as

}
