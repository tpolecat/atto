package atto
package syntax

import java.lang.String
import scala.{ StringContext, PartialFunction }
import scala.language.implicitConversions
import scalaz.syntax.Ops
import scalaz.\/
import atto.parser._

trait ParserOps[A] extends Ops[Parser[A]] {

  // Parsing

  def parse(b: String): ParseResult[A] = 
    Parser.parse(self, b)

  def parseOnly(b: String): ParseResult[A] = 
    Parser.parseOnly(self, b)

  // Combinator

  /** `a ~> b` is shorthand for `discardLeft(a, b)` */
  def ~>[B](n: Parser[B]): Parser[B] =
    combinator.discardLeft(self, n)

  /** `a <~ b` is shorthand for `discardRight(a, b)` */
  def <~[B](n: Parser[B]): Parser[A] =
    combinator.discardRight(self, n)

  /** `a ~ b` is shorthand for `andThen(a, b)` */
  def ~[B](n: Parser[B]): Parser[(A, B)] =
    combinator.andThen(self, n)

  /** `a | b` is shorthand for `orElse(a, b)` */
  def |[B >: A](n: Parser[B]): Parser[B] =
    combinator.orElse(self, n)

  /** `a || b` is shorthand for `either(a, b)` */
  def ||[B](n: Parser[B]): Parser[\/[A, B]] =
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

}

trait ToParserOps {

  implicit def toParserOps[A](p: Parser[A]): ParserOps[A] =
    new ParserOps[A] {
      val self = p
    }

}