package docs

object next_steps {
val _ = {

  //#logdata
  val logData =
   """|2013-06-29 11:16:23 124.67.34.60 keyboard
      |2013-06-29 11:32:12 212.141.23.67 mouse
      |2013-06-29 11:33:08 212.141.23.67 monitor
      |2013-06-29 12:12:34 125.80.32.31 speakers
      |2013-06-29 12:51:50 101.40.50.62 keyboard
      |2013-06-29 13:10:45 103.29.60.13 mouse
      |""".stripMargin
  //#logdata

  //#imports
  import atto._, Atto._
  import cats.implicits._
  //#imports

  //#ubyte
  case class UByte(toByte: Byte) {
    override def toString: String = (toByte.toInt & 0xFF).toString
  }

  val ubyte: Parser[UByte] = {
    int.filter(n => n >= 0 && n < 256) // ensure value is in [0 .. 256)
      .map(n => UByte(n.toByte))      // construct our UByte
      .namedOpaque("UByte")           // give our parser a name
  }
  //#ubyte

  //#ubyte-parse
  ubyte.parseOnly("foo")
  // res0: ParseResult[UByte] = Fail(foo,List(),Failure reading:UByte)

  ubyte.parseOnly("-42")
  // res1: ParseResult[UByte] = Fail(,List(),Failure reading:UByte)

  ubyte.parseOnly("255") // ok!
  // res2: ParseResult[UByte] = Done(,255)
  //#ubyte-parse

  //#ip
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
  //#ip

  //#ip-parse
  ip parseOnly "foo.bar"
  // res3: ParseResult[IP] = Fail(foo.bar,List(),Failure reading:UByte)

  ip parseOnly "128.42.42.1"
  // res4: ParseResult[IP] = Done(,IP(128,42,42,1))

  ip.parseOnly("128.42.42.1").option
  // res5: Option[IP] = Some(IP(128,42,42,1))
  //#ip-parse

  val _ = {

    //#ip-2
    val dot: Parser[Char] =  char('.')

    val ip: Parser[IP] =
      for {
        a <- ubyte <~ dot
        b <- ubyte <~ dot
        c <- ubyte <~ dot
        d <- ubyte
      } yield IP(a, b, c, d)
    //#ip-2

    //#ip-parse-2
    ip.parseOnly("128.42.42.1").option
    // res6: Option[IP] = Some(IP(128,42,42,1))
    //#ip-parse-2

    //#ip-named
    val ip2 = ip named "ip-address"
    val ip3 = ip namedOpaque "ip-address" // difference is illustrated below
    //#ip-named

    //#ip-named-parse
    ip2 parseOnly "foo.bar"
    // res7: ParseResult[IP] = Fail(foo.bar,List(ip-address),Failure reading:UByte)

    ip3 parseOnly "foo.bar"
    // res8: ParseResult[IP] = Fail(foo.bar,List(),Failure reading:ip-address)
    //#ip-named-parse

    //#ip-4
    val ubyteDot = ubyte <~ dot // why not?
    val ip4 = (ubyteDot, ubyteDot, ubyteDot, ubyte).mapN(IP.apply) named "ip-address"
    //#ip-4

    //#ip-4-parse
    ip4.parseOnly("128.42.42.1").option
    // res9: Option[IP] = Some(IP(128,42,42,1))
    //#ip-4-parse

    //#ip-4-parse-either
    ip4.parseOnly("abc.42.42.1").either
    // res10: Either[String,IP] = Left(Failure reading:UByte)

    ip4.parseOnly("128.42.42.1").either
    // res11: Either[String,IP] = Right(IP(128,42,42,1))
    //#ip-4-parse-either

    //#data-types
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
    //#data-types

    //#fixed
    def fixed(n:Int): Parser[Int] =
      count(n, digit).map(_.mkString).flatMap { s =>
        try ok(s.toInt) catch { case e: NumberFormatException => err(e.toString) }
      }
    //#fixed

    //#log
    val date: Parser[Date] =
      (fixed(4) <~ char('-'), fixed(2) <~ char('-'), fixed(2)).mapN(Date.apply)

    val time: Parser[Time] =
      (fixed(2) <~ char(':'), fixed(2) <~ char(':'), fixed(2)).mapN(Time.apply)

    val dateTime: Parser[DateTime] =
      (date <~ char(' '), time).mapN(DateTime.apply)

    val product: Parser[Product] = {
      string("keyboard").map(_ => Keyboard : Product) |
      string("mouse")   .map(_ => Mouse : Product)    |
      string("monitor") .map(_ => Monitor : Product)  |
      string("speakers").map(_ => Speakers : Product)
    }

    val logEntry: Parser[LogEntry] =
      (dateTime <~ char(' '), ip <~ char(' '), product).mapN(LogEntry.apply)

    val log: Parser[Log] =
      sepBy(logEntry, char('\n'))
    //#log

    //#log-parse
    (log parseOnly logData).option.foldMap(_.mkString("\n"))
    // res12: String =
    //   LogEntry(DateTime(Date(2013,6,29),Time(11,16,23)),IP(124,67,34,60),Keyboard)
    //   LogEntry(DateTime(Date(2013,6,29),Time(11,32,12)),IP(212,141,23,67),Mouse)
    //   LogEntry(DateTime(Date(2013,6,29),Time(11,33,8)),IP(212,141,23,67),Monitor)
    //   LogEntry(DateTime(Date(2013,6,29),Time(12,12,34)),IP(125,80,32,31),Speakers)
    //   LogEntry(DateTime(Date(2013,6,29),Time(12,51,50)),IP(101,40,50,62),Keyboard)
    //   LogEntry(DateTime(Date(2013,6,29),Time(13,10,45)),IP(103,29,60,13),Mouse)
    //#log-parse

  }
 ()
}
}