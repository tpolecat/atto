package atto
import Atto._

import cats.implicits._
import scala.util.Random

import org.scalacheck._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
object CharacterTest extends Properties("Character") {
  import Prop._

  property("satisfy") = forAll { (w: Char, s: String) =>
    satisfy(_ <= w).parse(w +: s).option === Some(w)
  }

  property("oneOf") = forAll { (s: String) => s.nonEmpty ==> {
    val randomChar = s(Random.nextInt(s.size))
    oneOf(s).parse(randomChar.toString).option === Some(randomChar)
  }}

  property("noneOf") = forAll { (s: String) => s.nonEmpty ==> {
    val randomChar = s(Random.nextInt(s.size))
    noneOf(s).parse(randomChar.toString).option === None
  }}

  property("char") = forAll { (w: Char, s: String) =>
    char(w).parse(w +: s).option === Some(w)
  }

  property("anyChar") = forAll { (s: String) =>
    val p = anyChar.parse(s).option
    if (s.isEmpty) p === None else p === Some(s.head)
  }

  property("notChar") = forAll { (w: Char, s: String) => (!s.isEmpty) ==> {
    val v = s.head
    notChar(w).parse(s).option === (if (v === w) None else Some(v))
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
    optElem(c => Some(c).filter(_ < d)).parseOnly(c.toString).option ===
      Some(c).filter(_ < d)
  }

  property("optElem + many") = forAll { (s: String, c: Char) =>
    val p = many(optElem(ch => Some(ch).filter(_ < c)))
    p.parseOnly(s).option === Some(s.toList.takeWhile(_ < c))
  }

  property("decimalDigit") = forAll { (c: Char) =>
    decimalDigit.parseOnly(c.toString).option ===
      (if("0123456789".contains(c)) Some(c) else None)
  }

  property("binaryDigit") = forAll { (c: Char) =>
    binaryDigit.parseOnly(c.toString).option ===
      (if(c == '0' || c == '1') Some(c) else None)
  }

  property("octalDigit") = forAll { (c: Char) =>
    octalDigit.parseOnly(c.toString).option ===
      (if("01234567".contains(c)) Some(c) else None)
  }

  property("Character.java derivatives") = forAll { (c: Char) =>
    List[(Parser[Char], Function[Char, Boolean])](
      (digit, _.isDigit),
      (letter, _.isLetter),
      (letterOrDigit, _.isLetterOrDigit),
      (lower, _.isLower),
      (spaceChar, _.isSpaceChar),
      (upper, _.isUpper),
      (whitespace, _.isWhitespace)).map {
      case (parser, predicate) =>
        parser.parseOnly(c.toString).option ===
          (if (predicate(c)) Some(c) else None)
    }.forall(_ == true)
  }

  property("skip") = forAll { (c: Char, b: Boolean) =>
    val predicate = (cc: Char) => if(b) c == cc else c != cc
    skip(predicate).parseOnly(c.toString).option ===
      (if (predicate(c)) Some(()) else None)
  }

  property("horizontalWhitespace") = forAll(Gen.oneOf(Generators.horizontalWhitespace, Arbitrary.arbitrary[Char])) { (c: Char) =>
    //println(f"CHK(${c.toInt}%04x) TEST(${CharClass.horizontalWhitespace.unapplySeq(c).flatMap(_.headOption)}) PARSE(${horizontalWhitespace.parseOnly(c.toString).option})")
    horizontalWhitespace.parseOnly(c.toString).option === CharClass.horizontalWhitespace.unapplySeq(c).flatMap(_.headOption)
  }
}

private object CharClass {
  val horizontalWhitespace = raw"(\h)".r    // " \t\xA0\u1680\u180e\u2000-\u200a\u202f\u205f\u3000"
  val horizontalWhitespaceChars =
    " \u0009\u00A0\u1680\u180e\u202f\u205f\u3000".toVector ++ ('\u2000' to '\u200a').toVector
}
