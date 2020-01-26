package docs

object first_steps {
val _ = {

  //#imports
  import atto._, Atto._
  import cats.implicits._
  //#imports

  //#abc
  int parseOnly "123abc"
  // res0: atto.ParseResult[Int] = Done(abc,123)
  //#abc

  //#int
  int
  // res1: atto.Parser[Int] = int
  //#int

  //#letter
  letter
  // res2: atto.Parser[Char] = letter
  //#letter

  //#letter-parse-1
  letter.parse("x")
  // res3: atto.ParseResult[Char] = Done(,x)

  letter.parse("xyz")
  // res4: atto.ParseResult[Char] = Done(yz,x)
  //#letter-parse-1

  //#letter-parse-2
  letter.parse("1")
  // res5: atto.ParseResult[Char] = Fail(1,List(),Failure reading:letter)
  //#letter-parse-2

  //#letter-parse-3
  letter.parse("")
  // res6: atto.ParseResult[Char] = Partial(atto.Parser$Internal$Partial$$Lambda$6075/657171239@12d6096a)

  letter.parse("").feed("abc")
  // res7: atto.ParseResult[Char] = Done(bc,a)
  //#letter-parse-3

  //#many-letter-parse-1
  many(letter).parse("abc")
  // res8: atto.ParseResult[List[Char]] = Partial(atto.Parser$Internal$Partial$$Lambda$6457/959637437@6f0ca749)

  many(letter).parse("abc").feed("def")
  // res9: atto.ParseResult[List[Char]] = Partial(atto.Parser$Internal$Partial$$Lambda$6457/959637437@3d8407c0)
  //#many-letter-parse-1

  //#many-letter-parse-2
  many(letter).parse("abc").feed("def").done
  // res10: atto.ParseResult[List[Char]] = Done(,List(a, b, c, d, e, f))
  //#many-letter-parse-2

  //#many-letter-map
  many(letter).map(_.mkString).parse("abc").feed("def").done
  // res11: atto.ParseResult[String] = Done(,abcdef)
  //#many-letter-map

  //#twiddle-1
  letter ~ digit
  // res12: atto.Parser[(Char, Char)] = (letter) ~ digit

  (letter ~ digit).parse("a1")
  // res13: atto.ParseResult[(Char, Char)] = Done(,(a,1))

  (many(letter) ~ many(digit)).parse("aaa")
  // res14: atto.ParseResult[(List[Char], List[Char])] = Partial(atto.Parser$Internal$Partial$$Lambda$6583/1845211818@1ddc9a8d)

  (many(letter) ~ many(digit)).parse("aaa").feed("bcd123").done
  // res15: atto.ParseResult[(List[Char], List[Char])] = Done(,(List(a, a, a, b, c, d),List(1, 2, 3)))

  (many(letter) ~ many(digit)).map { case (a, b) => a ++ b } .parse("aaa").feed("bcd123").done
  // res16: atto.ParseResult[List[Char]] = Done(,List(a, a, a, b, c, d, 1, 2, 3))
  //#twiddle-1

  //#twiddle-2
  (letter ~ int ~ digit ~ byte)
  // res17: atto.Parser[(((Char, Int), Char), Byte)] = (((letter) ~ int) ~ digit) ~ byte
  //#twiddle-2

  //#applicative
  (many(letter), many(digit)).mapN(_ ++ _).parse("aaa").feed("bcd123").done
  // res18: atto.ParseResult[List[Char]] = Done(,List(a, a, a, b, c, d, 1, 2, 3))
  //#applicative

  //#monad
  val p = for { n <- int; c <- take(n) } yield c
  // p: atto.Parser[String] = (int) flatMap ...

  p.parse("3abcdef")
  // res19: atto.ParseResult[String] = Done(def,abc)

  p.parse("4abcdef")
  // res20: atto.ParseResult[String] = Done(ef,abcd)
  //#monad

}
}