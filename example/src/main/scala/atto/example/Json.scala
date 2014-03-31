package atto.example

import scalaz.syntax.functor._
import scalaz.std.list._

import atto._
import Atto._

object JsonExample extends Whitespace {

  // Json AST
  sealed trait JValue
  case object JNull extends JValue
  case class JBoolean(value: Boolean) extends JValue 
  case class JString(value: String) extends JValue 
  case class JNumber(value: Double) extends JValue 
  case class JArray(values: List[JValue]) extends JValue 
  case class JObject(values: List[(String, JValue)]) extends JValue 

  // Bracketed, comma-separated sequence, internal whitespace allowed 
  def seq[A](open: Char, p: Parser[A], close: Char): Parser[List[A]] =
    char(open).t ~> sepByT(p, char(',')) <~ char(close)

  // Colon-separated pair, internal whitespace allowed
  lazy val pair: Parser[(String, JValue)] =
    pairByT(stringLiteral, char(':'), jexpr)

  // Json Expression
  lazy val jexpr: Parser[JValue] = delay { 
    string("null")       ^^^ JNull           |
    string("true")       ^^^ JBoolean(true)  |
    string("false")      ^^^ JBoolean(false) |
    double               ^^  JNumber         |
    stringLiteral        ^^  JString         |
    seq('[', jexpr, ']') ^^  JArray          |
    seq('{', pair,  '}') ^^  JObject
  }

}


trait Whitespace {

  // Delimited list
  def sepByT[A](a: Parser[A], b: Parser[_]): Parser[List[A]] = 
    sepBy(a.t, b.t)

  // Delimited pair, internal whitespace allowed
  def pairByT[A,B](a: Parser[A], delim: Parser[_], b: Parser[B]): Parser[(A,B)] =
    pairBy(a.t, delim.t, b)

}

