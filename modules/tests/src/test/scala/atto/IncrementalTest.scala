package atto
import Atto._

import org.scalacheck._
import scalaz._
import atto.compat.scalaz._

object IncrementalTest extends Properties("Incremental") {
  import Prop._

  // list of ints chunked arbitrarily
  property("incremental/1") = forAll { (n: Int, ns0: List[Int], c: Char, s: String) =>
    val sep = s + s + s + c
    !sep.exists(_.isDigit) ==> { 
      val ns = n :: ns0 ++ ns0 ++ ns0 
      val p = sepBy(int, string(sep))
      val s = ns.mkString(sep)
      val c = s.grouped(1 max (n % s.length).abs).toList
      (p.parse("") /: c)(_ feed _).done match {
        case ParseResult.Done("", `ns`) => true
        case _ => false
      }
    }
  }

}

