package atto
package syntax

import eu.timepit.refined.api.{Refined, Validate}
import eu.timepit.refined.refineV

trait ParserRefinedOps[T] {
  val self: Parser[T]
  import atto.parser.combinator.{err, ok}

  def refined[P](implicit ev: Validate[T, P]): Parser[T Refined P] =
    self.flatMap(refineV(_).fold(err, ok))
}

trait ToParserRefinedOps {

  implicit def toParserRefinedOps[T](p: Parser[T]): ParserRefinedOps[T] =
    new ParserRefinedOps[T] {
      val self: Parser[T] = p
    }

}

object refined extends ToParserRefinedOps
