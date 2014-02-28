package atto.syntax.stream

import scala.language.implicitConversions
import scalaz.syntax.Ops
import atto._

trait ParserOps[A] extends Ops[Parser[A]] {


}

trait ToParserOps {

  // N.B. ensure this name doesn't shadow the one from core :-\
  implicit def toStreamParserOps[A](p: Parser[A]): ParserOps[A] =
    new ParserOps[A] {
      val self = p
    }

}