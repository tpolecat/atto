package atto

import org.scalacheck.Gen

object Generators {
  val whitespace: Gen[Char] = Gen.oneOf('\n', ' ', '\t')

  val horizontalWhitespace = Gen.oneOf(CharClass.horizontalWhitespaceChars)
}
