package atto
package parser

import _root_.spire.math._
import atto.syntax.parser._
import scalaz.Order
import scalaz.syntax.order._

trait Spire {
  import combinator._
  import numeric._

  /** Parser for an unsigned byte (range-checked). */
  val ubyte: Parser[UByte] = 
    narrow[Int, UByte](int, UByte.MinValue.toInt, UByte.MaxValue.toInt, UByte(_))

  /** Parser for an unsigned short (range-checked). */
  val ushort: Parser[UShort] = 
    narrow[Int, UShort](int, UShort.MinValue.toInt, UShort.MaxValue.toInt, UShort(_))

  /** Parser for an unsigned int (range-checked). */
  val uint: Parser[UInt] = 
    narrow[Long, UInt](long, UInt.MinValue.toLong, UInt.MaxValue.toLong, UInt(_))

  /** Parser for an unsigned long (range-checked). */
  val ulong: Parser[ULong] = 
    narrow[BigInt, ULong](bigInt, ULong.MinValue.toBigInt, ULong.MaxValue.toBigInt, n => ULong(n.toLong))

  ////// Helpers

  private def narrow[A : Order, B](pa: Parser[A], min: A, max: A, f: A => B): Parser[B] = 
    pa.flatMap { n =>
      if (n >= min && n <= max) ok(f(n)) 
      else err(s"out of range: $n)")
    } as "ubyte"

}

