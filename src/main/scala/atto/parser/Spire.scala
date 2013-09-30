package atto
package parser

import _root_.spire.math._
import atto.syntax.parser._

trait Spire {
  import combinator._
  import numeric._

  val ubyte: Parser[UByte] = 
    int.flatMap { n =>
      if (n >= UByte.MinValue.toInt && n <= UByte.MaxValue.toInt) ok(UByte(n)) 
      else err(s"out of range: $n)")
    } as "ubyte"

}