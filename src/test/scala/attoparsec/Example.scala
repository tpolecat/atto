package attoparsec

import scalaz._
import scalaz.effect._
import Scalaz._
import spire.math.UByte

// This is adapted from https://www.fpcomplete.com/school/text-manipulation/attoparsec
object Example extends App with SpireParsers {
  import Parser._
  import IO._

  // IP Address
  case class IP(a: UByte, b: UByte, c: UByte, d: UByte) 

  // As a first pass we can parse an IP address in the form 128.42.30.1 by using the `ubyte` and 
  // `char` parsers directly, in a `for` comprehension.
  val ip: Parser[IP] =
    for {
      a <- ubyte
      _ <- char('.')
      b <- ubyte
      _ <- char('.')
      c <- ubyte
      _ <- char('.')
      d <- ubyte
    } yield IP(a, b, c, d)

  // Try it!
  println(ip parseOnly "foo.bar") // Fail(foo.bar,List(ubyte, int, long),Failure reading:digit)
  println(ip parseOnly "128.42.42.1") // Done(,IP(128,42,42,1))
  println(ip.parseOnly("128.42.42.1").option) // Some(IP(128,42,42,1)

  // Let's factor out the dot.
  val dot: Parser[Char] =  char('.')

  // The `<~` and `~>` combinators combine two parsers sequentially, discarding the value produced by
  // the parser on the `~` side. We can use this to simplify our comprehension a bit.
  val ip1: Parser[IP] =
    for { 
      a <- ubyte <~ dot
      b <- ubyte <~ dot
      c <- ubyte <~ dot
      d <- ubyte
    } yield IP(a, b, c, d)

  // Try it!
  println(ip1.parseOnly("128.42.42.1").option) // Some(IP(128,42,42,1)

  // We can name our parser, which provides slightly more enlightening failure messages
  val ip2 = ip1 as "ip-address"
  val ip3 = ip1 asOpaque "ip-address" // difference is illustrated below

  // Try it!
  println(ip2 parseOnly "foo.bar") // Fail(foo.bar,List(ip-address, ubyte, int, long),Failure reading:digit)
  println(ip3 parseOnly "foo.bar") // Fail(foo.bar,List(),Failure reading:ip-address)

  // Since nothing that occurs on the left-hand side of our <- appears on the right-hand side, we
  // don't actually need a monad; we can use applicative syntax.
  val ubyteDot = ubyte <~ char('.') // why not?
  val ip4 = (ubyteDot |@| ubyteDot |@| ubyteDot |@| ubyte)(IP.apply) as "ip-address"

  // Try it!
  println(ip4.parseOnly("128.42.42.1").option) // Some(IP(128,42,42,1)

  // We might prefer to get some information about failure, so `either` is an, um, option
  println(ip4.parseOnly("abc.42.42.1").either) // -\/(Failure reading:digit)
  println(ip4.parseOnly("128.42.42.1").either) // \/-(IP(128,42,42,1))

}

trait SpireParsers {
  import Parser._

  // Main source doesn't depend on spire, so we define this here. Nice example of how easy it is to
  // take an existing parser and further constrain it.
  val ubyte: Parser[UByte] = 
    int.flatMap { n =>
      if (n >= UByte.MinValue.toInt && n <= UByte.MaxValue.toInt) ok(UByte(n)) 
      else err(s"ubyte (out of range: $n)")
    } as "ubyte"

}