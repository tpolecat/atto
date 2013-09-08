package atto
package syntax

import scala.language.implicitConversions
import scalaz.syntax.Ops
import atto._

trait ParserOps[A] extends Ops[Parser[A]] {

  // Parsing

  def parse(b: String): ParseResult[A] = 
    Parser.parse(self, b)

  def parseOnly(b: String): ParseResult[A] = 
    Parser.parseOnly(self, b)

  // Combinators

  def ~>[B](n: Parser[B]): Parser[B] =
    Combinators.discardLeft(self, n)

  def <~[B](n: Parser[B]): Parser[A] =
    Combinators.discardRight(self, n)

  def ~[B](n: Parser[B]): Parser[(A, B)] =
    Combinators.andThen(self, n)

  def |[B >: A](n: Parser[B]): Parser[B] =
    Combinators.orElse(self, n)

  def ||[B](n: Parser[B]): Parser[Either[A, B]] =
    Combinators.either(self, n)

  def as(s: => String): Parser[A] = 
    Combinators.named(self, s)

  def asOpaque(s: => String): Parser[A] = 
    Combinators.namedOpaque(self, s)

}

trait ToParserOps {

  implicit def ToParserOps[A](p: Parser[A]): ParserOps[A] =
    new ParserOps[A] {
      val self = p
    }

}