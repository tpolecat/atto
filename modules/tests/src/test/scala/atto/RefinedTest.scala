package atto

import atto.Atto._
import atto.syntax.refined._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.MatchesRegex
import eu.timepit.refined.W
import cats.implicits._
import org.scalacheck._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
object RefinedTest extends Properties("Refined") {
  import Prop._

  property("refined") = forAll { (n: Long) =>
    type Hex = MatchesRegex[W.`"[0-9a-fA-F]+"`.T]
    val hexStr = n.toHexString
    stringOf(hexDigit).refined[Hex].parseOnly(hexStr).option.map(_.value) === Some(hexStr)
  }
}
