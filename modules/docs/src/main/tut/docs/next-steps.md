---
layout: docs
title: Parsing Log Entries
---

### {{page.title}}

Our goal for this chapter is to parse the following log data into structured values. This is taken from a nice tutorial over at [FP Complete](https://www.fpcomplete.com/school/text-manipulation/attoparsec).

```tut:silent
val logData =
  """|2013-06-29 11:16:23 124.67.34.60 keyboard
     |2013-06-29 11:32:12 212.141.23.67 mouse
     |2013-06-29 11:33:08 212.141.23.67 monitor
     |2013-06-29 12:12:34 125.80.32.31 speakers
     |2013-06-29 12:51:50 101.40.50.62 keyboard
     |2013-06-29 13:10:45 103.29.60.13 mouse
     |""".stripMargin
```

We'll use the same imports as before.

```tut:silent
import atto._, Atto._
```

This data contains IP addresses, which in turn contain unsigned bytes. So our first order of business is figuring out how to parse these.

### Parsing Unsigned Bytes and IP Addresses

IP addresses contain unsigned bytes, which we don't have in Scala. So the first thing we'll do is create a data type wrapping a signed byte, and then write a parser for it.

```tut:silent
case class UByte(toByte: Byte) {
  override def toString: String = (toByte.toInt & 0xFF).toString
}

val ubyte: Parser[UByte] = {
  int.filter(n => n >= 0 && n < 256) // ensure value is in [0 .. 256)
     .map(n => UByte(n.toByte))      // construct our UByte
     .namedOpaque("UByte")           // give our parser a name
}
```

It works!

```tut
ubyte.parseOnly("foo")
ubyte.parseOnly("-42")
ubyte.parseOnly("255") // ok!
```

We can now define our `IP` data type and a parser for it. As a first pass we can parse an IP address in the form 128.42.30.1 by using the `ubyte` and `char` parsers directly, in a `for` comprehension.

```tut:silent
case class IP(a: UByte, b: UByte, c: UByte, d: UByte)

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
```

It works!

```tut
ip parseOnly "foo.bar"
ip parseOnly "128.42.42.1"
ip.parseOnly("128.42.42.1").option
```

The `<~` and `~>` combinators combine two parsers sequentially, discarding the value produced by
the parser on the `~` side. We can factor out the dot and use `<~` to simplify our comprehension a bit.

```tut:silent
val dot: Parser[Char] =  char('.')

val ip: Parser[IP] =
  for {
    a <- ubyte <~ dot
    b <- ubyte <~ dot
    c <- ubyte <~ dot
    d <- ubyte
  } yield IP(a, b, c, d)
```

And it still works.

```tut
ip.parseOnly("128.42.42.1").option
```

We can name our parser, which provides slightly more enlightening failure messages

```tut:silent
val ip2 = ip named "ip-address"
val ip3 = ip namedOpaque "ip-address" // difference is illustrated below
```

Thus.

```tut
ip2 parseOnly "foo.bar"
ip3 parseOnly "foo.bar"
```

Since nothing that occurs on the right-hand side of our <- appears on the left-hand side, we
don't actually need a monad; we can use applicative syntax here.

```tut:silent
val ubyteDot = ubyte <~ dot // why not?
val ip4 = (ubyteDot |@| ubyteDot |@| ubyteDot |@| ubyte)(IP.apply) named "ip-address"
```

And it still works.

```tut
ip4.parseOnly("128.42.42.1").option
```

We might prefer to get some information about failure, so `either` is an, um, option.

```tut
ip4.parseOnly("abc.42.42.1").either
ip4.parseOnly("128.42.42.1").either
```

Ok, so we can parse IP addresses now. Let's move on to the log.

### Parsing Log Entries

Here are our log entries defined in `logData` above.

```tut:evaluated:plain
println(logData)
```

And some data types for the parsed data.

```tut:silent
case class Date(year: Int, month: Int, day: Int)
case class Time(hour: Int, minutes: Int, seconds: Int)
case class DateTime(date: Date, time: Time)

sealed trait Product // Products are an enumerated type
case object Mouse extends Product
case object Keyboard extends Product
case object Monitor extends Product
case object Speakers extends Product

case class LogEntry(entryTime: DateTime, entryIP: IP, entryProduct: Product)
type Log = List[LogEntry]
```

There's no built-in parser for fixed-width ints, so we can just make one. We parse some number of digits and parse them as an `Int`, handling the case where the value is too large by flatmapping to `ok` or `err`.

```tut:silent
def fixed(n:Int): Parser[Int] =
  count(n, digit).map(_.mkString).flatMap { s =>
    try ok(s.toInt) catch { case e: NumberFormatException => err(e.toString) }
  }
```

Now we have what we need to put the log parser together.

```tut:silent
val date: Parser[Date] =
  (fixed(4) <~ char('-') |@| fixed(2) <~ char('-') |@| fixed(2))(Date.apply)

val time: Parser[Time] =
  (fixed(2) <~ char(':') |@| fixed(2) <~ char(':') |@| fixed(2))(Time.apply)

val dateTime: Parser[DateTime] =
  (date <~ char(' ') |@| time)(DateTime.apply)

val product: Parser[Product] = {
  string("keyboard").map(_ => Keyboard : Product) |
  string("mouse")   .map(_ => Mouse : Product)    |
  string("monitor") .map(_ => Monitor : Product)  |
  string("speakers").map(_ => Speakers : Product)
}

val logEntry: Parser[LogEntry] =
  (dateTime <~ char(' ') |@| ip <~ char(' ') |@| product)(LogEntry.apply)

val log: Parser[Log] =
  sepBy(logEntry, char('\n'))
```

It works!

```tut
(log parseOnly logData).option.foldMap(_.mkString("\n"))
```
