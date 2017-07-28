package atto
import Atto._

// compilation tests to verify that inference works for compat layers
object CompatTest {

  object ScalazCompat {

    import scalaz._, Scalaz._
    import atto.compat.scalaz._

    {
      val a = (null : ParseResult[Int]).either
      val b: String \/ Int = a
      b
    }

    {
      val a = many1(int)
      val b: Parser[NonEmptyList[Int]] = a
      b
    }

    {
      val c = choice(List[Parser[Int]]())
      c
    }

  }

  object StdlibCompat {

    import atto.compat.stdlib._

    {
      val a = (null : ParseResult[Int]).either
      val b: Either[String, Int] = a
      b
    }

    {
      val a = many1(int)
      val b: Parser[(Int, List[Int])] = a
      b
    }

    {
      val c = choice(List[Parser[Int]]())
      c
    }

  }

  object CatsCompat {

    import cats.data._, cats.implicits._
    import atto.compat.cats._

    {
      val a = (null : ParseResult[Int]).either
      val b: Either[String, Int] = a
      b
    }

    {
      val a = many1(int)
      val b: Parser[NonEmptyList[Int]] = a
      b
    }

    {
      val c = choice(List[Parser[Int]]())
      c
    }

  }

}
