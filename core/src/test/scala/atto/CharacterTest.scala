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

  property("charRange") = 
    charRange('a' to 'z', '0' to '9').parseOnly("3").option == Some('3') &&
    charRange('a' to 'z', '0' to '9').parseOnly("x").option == Some('x') &&
    charRange('a' to 'z', '0' to '9').parseOnly("!").option == None

}

