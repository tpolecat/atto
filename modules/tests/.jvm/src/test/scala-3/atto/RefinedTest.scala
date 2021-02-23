package atto

import atto.Atto._
import atto.syntax.refined._
import eu.timepit.refined.string.MatchesRegex
import cats.implicits._
import org.scalacheck._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
object RefinedTest extends Properties("Refined") {
  import Prop._

  property("refined success") = forAll { (n: Long) =>
    type Hex = MatchesRegex["[0-9a-fA-F]+"]
    val hexStr = n.toHexString
    stringOf(hexDigit).refined[Hex].parseOnly(hexStr).option.map(_.value) === Some(hexStr)
  }

  property("refined error") = forAll { (n: Long) =>
    type Alpha = MatchesRegex["[e-z]+"]
    stringOf(anyChar).refined[Alpha].parseOnly(n.toString).either.swap.toOption ===
      Some(s"""Predicate failed: "$n".matches("[e-z]+").""")
  }
}
