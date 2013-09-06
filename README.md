
scala-attoparsec
================

Forked from kmett, updated for scala 2.10 and scalaz 7. In progress.

```scala

scala> import attoparsec._; import Parser._
import attoparsec._
import Parser._

scala> case class IP(a: Int, b: Int, c: Int, d:Int)
defined class IP

scala> val dot = char('.')
dot: attoparsec.Parser[Char] = '.'

scala> val ip = for { 
     |   a <- int
     |   _ <- dot
     |   b <- int
     |   _ <- dot
     |   c <- int
     |   _ <- dot
     |   d <- int 
     | } yield IP(a, b, c, d)
ip: attoparsec.Parser[IP] = (int) flatMap ...

scala> ip parseOnly "123.87.69.9 and some more text"
res1: attoparsec.ParseResult[IP] = Done( and some more text,IP(123,87,69,9))

scala> ip parseOnly "foo"
res2: attoparsec.ParseResult[IP] = Fail(foo,List(int, long),Failure reading:digit)

```


