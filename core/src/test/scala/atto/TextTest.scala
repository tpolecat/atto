package atto
import Atto._

import org.scalacheck._
import scalaz.\/._

object TextTest extends Properties("Text") {
  import Prop._
  import Parser._

  property("stringOf") = forAll { (s: String) =>
    stringOf(elem(c => s.exists(_ == c))).parseOnly(s).option == Some(s)
  }

  property("stringOf1") = forAll { (s: String) =>
    stringOf1(elem(c => s.exists(_ == c))).parseOnly(s).option ==
      Some(s).filterNot(_.isEmpty)
  }

  property("take") = forAll { (s: String, n: Int) =>
    n > 1 ==> {
      take(n).parseOnly(s) match {
        case ParseResult.Done(h, t) => (h, t) == s.splitAt(n)
        case ParseResult.Fail(`s`, _, _) if n > s.length => true
        case _ => false
      }
    }
  }

  property("string") = forAll { (s: String, t: String) =>
    string(s).parse(s ++ t).option == Some(s)
  }

  property("takeCount") = forAll { (k: Int, s: String) => (k >= 0) ==> {
    take(k).parse(s).option match {
      case None => k > s.length
      case Some(_) => k <= s.length
    }
  }}

  property("takeWhile") = forAll { (w: Char, s: String) =>
    val (h, t) = s.span(_ == w)
    (for {
      hp <- takeWhile(_ == w)
      tp <- takeText
    } yield (hp, tp)).parseOnly(s).either == right((h, t))
  }

  property("takeWhile1") = forAll { (w: Char, s: String) =>
    val sp = w +: s
    val (h, t) = sp span (_ <= w)
    (for {
      hp <- takeWhile1(_ <= w)
      tp <- takeText
    } yield (hp, tp)).parseOnly(sp).either == right((h, t))
  }

  property("takeWhile1/empty") =
    takeWhile1(_ => true).parse("").option == None

  // scan

  property("stringLiteral") = forAll { (s: String, t: String) =>
    stringLiteral.parseOnly(quote(s) + t) == ParseResult.Done(t, s)
  }

  def quote(s: String) = s.map {
      case '"' => "\\\""
      case '\\' => "\\\\"
      case '/' => "\\/"
      case '\b' => "\\b"
      case '\f' => "\\f"
      case '\n' => "\\n"
      case '\r' => "\\r"
      case '\t' => "\\t"
      case c if ((c >= '\u0000' && c <= '\u001f') || (c >= '\u007f' && c <= '\u009f')) => "\\u%04x".format(c.toInt)
      case c => c
    }.mkString("\"", "", "\"")

  val whitespaces = Gen.listOf(Generators.whitespace).map(_.mkString)

  property("parens") = forAll(whitespaces, Gen.alphaStr, whitespaces) { (ws1, s, ws2) =>
    parens(string(s)).parse(s"($ws1$s$ws2)").option == Some(s)
  }

}
