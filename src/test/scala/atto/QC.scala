package atto
import Atto._

import org.scalacheck._
import scalaz.\/._

object QC extends Properties("Parser") {
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

  property("takeWhile1_empty") =
    takeWhile1(_ => true).parse("").option == None

  property("endOfInput") = forAll { (s: String) =>
    endOfInput.parseOnly(s).either == (if (s.isEmpty) right(()) else left("endOfInput"))
  }

}