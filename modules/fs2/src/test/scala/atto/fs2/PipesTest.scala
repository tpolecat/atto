package atto.fs2

import atto._
import Atto._
import cats.implicits._
import _root_.fs2._

import org.scalacheck._

@SuppressWarnings(Array("org.wartremover.warts.NonUnitStatements", "org.wartremover.warts.Any"))
object Fs2PipesTest extends Properties("Pipes") {
  import Prop._

  property("parse1 parses single value correctly") = forAll(Gen.posNum[Int]) { i: Int =>
    {
      val test = Stream.emit(i.show)
        .through(Pipes.parse1[Pure, Int](int))
        .compile
        .last
        .toRight("Nothing Emited")
        .flatMap(_.either)
      val expected = Either.right(i)
      test === expected
    }
  }

  property("parse1 outputs a failed parser on invalid input") = forAll(Gen.alphaStr) { s: String =>
    {
      val test = Stream.emit(s)
        .through(Pipes.parse1[Pure, Int](int))
        .compile
        .last
        .toRight("Nothing Emited")
        .flatMap(_.either)
      val expected = Either.left("Failure reading:bigInt")
      test === expected
    }
  }

  property("parseN outputs all values if good") = forAll(Gen.listOf(Gen.choose(0, 9))) {l : List[Int] =>
    val test = Stream.emits(l.map(_.show))
        .through(Pipes.parseN[Pure, Int](take(1).map(_.toInt))) //Known to be safe due to Gen used
        .toList

    test === l
  }

  property("parseN outputs values up until and Error") = forAll(Gen.listOf(Gen.choose(0, 9))) {l : List[Int] =>
    val listStrings = l match {
      case h :: hs => h.show :: " " :: hs.map(_.show)
      case Nil => List.empty[String]
    }
    val test = Stream.emits(listStrings)
        .through(Pipes.parseN[Pure, Int](int))
        .toList
        .headOption

    test === l.headOption
  }

  property("parseLenient recreates initial list on appropriate parser") = forAll { l: List[Int] =>
    val listStrings : List[String] = l.map(_.show)
    val stringStream: Stream[Pure, String] = Stream.emits(listStrings).intersperse(" ")
    stringStream.through(Pipes.parseLenient[Pure, Int](int)).toList === l
  }

  property("parseLenient ignoresInvalid") = forAll(Gen.alphaStr, Arbitrary.arbitrary[List[Int]]) { (s: String, l: List[Int]) =>
    val listStrings : List[String] = l.map(_.show)
    val stringStream: Stream[Pure, String] = Stream.emit(s) ++ Stream.emits(listStrings).intersperse(" ")
    stringStream.through(Pipes.parseLenient[Pure, Int](int)).toList === l
  }

}