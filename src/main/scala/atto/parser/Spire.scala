package atto
package parser

import _root_.spire.math._
import atto.syntax.parser._

trait Spire {
  import combinator._
  import numeric._

  // Main source doesn't depend on spire, so we define this here. Nice example of how easy it is to
  // take an existing parser and further constrain it.
  val ubyte: Parser[UByte] = 
    int.flatMap { n =>
      if (n >= UByte.MinValue.toInt && n <= UByte.MaxValue.toInt) ok(UByte(n)) 
      else err(s"ubyte (out of range: $n)")
    } as "ubyte"

}