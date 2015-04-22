package atto
import Atto._

import org.scalacheck._
import scalaz.\/._

object CharacterTest extends Properties("Character") {
  import Prop._
  import Parser._

  property("satisfy") = forAll { (w: Char, s: String) =>
    satisfy(_ <= w).parse(w +: s).option == Some(w)
  }

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

}

