package atto
import Atto._
import atto.compat.scalaz._

import scala.util.Random

import org.scalacheck._
import scalaz.\/._

object CharacterTest extends Properties("Character") {
  import Prop._
  import Parser._

  property("satisfy") = forAll { (w: Char, s: String) =>
    satisfy(_ <= w).parse(w +: s).option == Some(w)
  }

  property("oneOf") = forAll { (s: String) => s.nonEmpty ==> {
    val randomChar = s(Random.nextInt(s.size))
    oneOf(s).parse(randomChar.toString).option == Some(randomChar)
  }}

  property("noneOf") = forAll { (s: String, c: Char) => s.nonEmpty ==> {
    val randomChar = s(Random.nextInt(s.size))
    noneOf(s).parse(randomChar.toString).option == None
  }}

  property("char") = forAll { (w: Char, s: String) =>
    char(w).parse(w +: s).option == Some(w)
  }

  property("anyChar") = forAll { (s: String) =>
    val p = anyChar.parse(s).option
    if (s.isEmpty) p == None else p == Some(s.head)
  }

  property("notChar") = forAll { (w: Char, s: String) => (!s.isEmpty) ==> {
    val v = s.head
    notChar(w).parse(s).option == (if (v == w) None else Some(v))
  }}

  property("charRange") = forAll { (ps: List[(Char, Char)], c: Char) =>
    val rs = ps.map(p => p._1 to p._2)
    val in = rs.exists(_.contains(c))
    charRange(rs: _*).parseOnly(c.toString).option match {
      case Some(`c`) if in => true
      case None if !in => true
      case _ => false
    }
  }

  property("optElem") = forAll { (c: Char, d: Char) =>
    optElem(c => Some(c).filter(_ < d)).parseOnly(c.toString).option ==
      Some(c).filter(_ < d)
  }

  property("optElem + many") = forAll { (s: String, c: Char) =>
    val p = many(optElem(ch => Some(ch).filter(_ < c)))
    p.parseOnly(s).option == Some(s.toList.takeWhile(_ < c))
  }

}
