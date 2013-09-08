
atto
====

Scala port of `attoparsec`, forked from kmett. Progress thus far:

   * Updated to Scala 2.10 and scalaz 7.0
   * Implementation is now trampolined (no more stack overflows)
   * Additional combinators and parsing options
   * Beginnings of a tutorial

There are still no tests. So, um, need to do that.

quick start
-----------

Clone, build, and play around. That's as quick as it gets at the moment.

```scala

scala> import atto._; import Parser._
import atto._
import Parser._

scala> case class IP(a: Int, b: Int, c: Int, d:Int)
defined class IP

scala> val dot = char('.')
dot: atto.Parser[Char] = '.'

scala> val ip = for { 
     |   a <- int
     |   _ <- dot
     |   b <- int
     |   _ <- dot
     |   c <- int
     |   _ <- dot
     |   d <- int 
     | } yield IP(a, b, c, d)
ip: atto.Parser[IP] = (int) flatMap ...

scala> ip parseOnly "123.87.69.9 and some more text"
res1: atto.ParseResult[IP] = Done( and some more text,IP(123,87,69,9))

scala> ip parseOnly "foo"
res2: atto.ParseResult[IP] = Fail(foo,List(int, long),Failure reading:digit)

scala> many(char('a')) parseOnly List.fill(20000)("a").mkString // Trampolining
res1: atto.ParseResult[List[Char]] = Done(,List(a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, 
a, a,...

```

