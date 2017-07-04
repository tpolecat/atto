package atto
package syntax

import cats.data.NonEmptyList
import java.lang.String
import scala.language.implicitConversions
import atto.parser._

trait ParserOps[A] {
  val self: Parser[A]

  // Parsing

  def parse(b: String): ParseResult[A] =
    Parser.parse(self, b)

  def parseOnly(b: String): ParseResult[A] =
    Parser.parseOnly(self, b)

  // Text

  def token: Parser[A] =
    text.token(self)

  def parens: Parser[A] =
    text.parens(self)

  // Combinator

  /** `a ~> b` is shorthand for `discardLeft(a, b)` */
  def ~>[B](n: => Parser[B]): Parser[B] =
    combinator.discardLeft(self, n)

  /** `a <~ b` is shorthand for `discardRight(a, b)` */
  def <~[B](n: => Parser[B]): Parser[A] =
    combinator.discardRight(self, n)

  /** `a ~ b` is shorthand for `andThen(a, b)` */
  def ~[B](n: => Parser[B]): Parser[(A, B)] =
    combinator.andThen(self, n)

  /** `a | b` is shorthand for `orElse(a, b)` */
  def |[B >: A](n: => Parser[B]): Parser[B] =
    combinator.orElse(self, n)

  /** `a || b` is shorthand for `either(a, b)` */
  def ||[B](n: => Parser[B]): Parser[Either[A, B]] =
    combinator.either(self, n)

  /** `a -| f` is shorthand for `a map f` */
  def -|[B](f: A => B): Parser[B] =
    self map f

  /** `a >| b` is shorthand for `a map (_ => b)` */
  def >|[B](b: => B): Parser[B] =
    self map (_ => b)

  def named(s: => String): Parser[A] =
    combinator.named(self, s)

  def namedOpaque(s: => String): Parser[A] =
    combinator.namedOpaque(self, s)

  def collect[B](pf: PartialFunction[A,B]): Parser[B] =
    combinator.collect(self, pf)

  def sepBy[B](s: Parser[B]): Parser[List[A]] =
    combinator.sepBy(self, s)

  def sepBy1[B](s: Parser[B]): Parser[NonEmptyList[A]] =
    combinator.sepBy1(self, s)

  def attempt: Parser[A] =
    combinator.attempt(self)

  def skipMany: Parser[Unit] =
    combinator.skipMany(self)

  def skipMany1: Parser[Unit] =
    combinator.skipMany1(self)

  def skipManyN(n: Int): Parser[Unit] =
    combinator.skipManyN(n, self)

  def many: Parser[List[A]] =
    combinator.many(self)

  def many1: Parser[NonEmptyList[A]] =
    combinator.many1(self)

  def manyN(n: Int): Parser[List[A]] =
    combinator.manyN(n, self)
}

trait ToParserOps {

  implicit def toParserOps[A](p: Parser[A]): ParserOps[A] =
    new ParserOps[A] {
      val self = p
    }

}
