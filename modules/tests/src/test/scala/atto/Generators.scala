package atto

import org.scalacheck.Gen

object Generators {
  val whitespace: Gen[Char] =
    Gen.oneOf('\n', ' ', '\t')
}
