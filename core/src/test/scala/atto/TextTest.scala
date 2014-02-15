package atto
import Atto._

import org.scalacheck._
import scalaz.\/._

object TextTest extends Properties("Text") {
  import Prop._
  import Parser._

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

  property("endOfInput") = forAll { (s: String) =>
    endOfInput.parseOnly(s).either == (if (s.isEmpty) right(()) else left("endOfInput"))
  }

}