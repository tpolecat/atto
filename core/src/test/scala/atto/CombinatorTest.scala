package atto
import Atto._

import org.scalacheck._
import scalaz.\/._

object CombinatorTest extends Properties("Combinator") {
  import Prop._
  import Parser._

  property("sepBy") = {
    val p = sepBy(int, char(',') <~ many(spaceChar))
    p.parseOnly("").option == Some(List()) &&
    p.parseOnly("1").option == Some(List(1)) &&
    p.parseOnly("1,2").option == Some(List(1,2))
  }

  property("sepBy1") = {
    val p = sepBy1(int, char(',') <~ many(spaceChar))
    p.parseOnly("").option == None &&
    p.parseOnly("1").option == Some(List(1)) &&
    p.parseOnly("1,2").option == Some(List(1,2))
  }

}

