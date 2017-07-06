package atto
import Atto._

import cats._, cats.data._, cats.implicits._
import org.scalacheck._

object CombinatorTest extends Properties("Combinator") {
  import Prop._

  implicit val eqChar: Eq[Char] = Order[Char] // :-\

  property("ok") = forAll { (s: String, n: Int) =>
    ok(n).parseOnly(s) match {
      case ParseResult.Done(`s`, `n`) => true
      case _ => false
    }
  }

  property("err") = forAll { (s: String) =>
    err("oops").parseOnly(s) match {
      case ParseResult.Fail(`s`, _, _) => true
      case _ => false
    }
  }

  property("get") = forAll { (s: String) =>
    get.parseOnly(s) match {
      case ParseResult.Done(`s`, `s`) => true
      case _ => false
    }
  }

  property("pos") = forAll { (s: String) =>
    (s.length > 0) ==> {
      val simpleParser = for {
        _ <- take(s.length - 1)
        p <- pos
      } yield p

      simpleParser.parseOnly(s) match {
        case ParseResult.Done(_, position) => position == s.length - 1
        case _ => false
      }
    }
  }

  property("advance") = forAll { (s: String, x: Int) =>
    (x >= 0) ==> {
      advance(x).parseOnly(s) match {
        case ParseResult.Done(s, ()) => true
        case _ => false
      }
    }
  }

  // TODO: demandInput

  property("ensure") = forAll { (s: String, n: Int) =>
    (n >= 0) ==> {
      ensure(n).parseOnly(s) match {
        case ParseResult.Done(s, _) if n <= s.length => true
        case ParseResult.Fail(_, _, _) => true
        case _ => false
      }
    }
  }

  property("wantInput") = forAll { (s: String) =>
    wantInput.parseOnly(s).option == Some(!s.isEmpty)
  }

  property("endOfInput") = forAll { (s: String) =>
    endOfInput.parseOnly(s).option == (if (s.isEmpty) Some(()) else None)
  }

  // TODO: attempt

   property("discardLeft") = forAll { (n: Int, s: String) =>
    !(s.exists(_.isDigit)) ==> {
      discardLeft(int, string(s)).parseOnly(n + s) match {
        case ParseResult.Done("", `s`) => true
        case _ => false
      }
    }
  }

  property("discardRight") = forAll { (n: Int, s: String) =>
    !(s.exists(_.isDigit)) ==> {
      discardRight(string(s), int).parseOnly(s + n) match {
        case ParseResult.Done("", `s`) => true
        case _ => false
      }
    }
  }

  property("andThen") = forAll { (n: Int, s: String) =>
    !(s.exists(_.isDigit)) ==> {
      andThen(string(s), int).parseOnly(s + n) match {
        case ParseResult.Done("", (`s`, `n`)) => true
        case _ => false
      }
    }
  }

  property("orElse/left") = forAll { (n: Int, s: String) =>
    !(s.isEmpty || s.exists(_.isDigit)) ==> {
      val p1: Parser[Either[Int, String]] = string(s).map(Right(_))
      val p2: Parser[Either[Int, String]] = int.map(Left(_))
      orElse(p1, p2).parseOnly(n + s) match {
        case ParseResult.Done(`s`, Left(`n`)) => true
        case _ => false
      }
    }
  }

  property("orElse/right") = forAll { (n: Int, s: String) =>
    !(s.exists(_.isDigit)) ==> {
      val p1: Parser[Either[Int, String]] = string(s).map(Right(_))
      val p2: Parser[Either[Int, String]] = int.map(Left(_))
      orElse(p1, p2).parseOnly(s + n) match {
        case ParseResult.Done(_, Right(`s`)) => true
        case _ => false
      }
    }
  }

  property("either/left") = forAll { (n: Int, s: String) =>
    !(s.isEmpty || s.exists(_.isDigit)) ==> {
      either(int, string(s)).parseOnly(n + s) match {
        case ParseResult.Done(`s`, Left(`n`)) => true
        case _ => false
      }
    }
  }

  property("either/right") = forAll { (n: Int, s: String) =>
    !(s.isEmpty || s.exists(_.isDigit)) ==> {
      either(int, string(s)).parseOnly(s + n) match {
        case ParseResult.Done(_, Right(`s`)) => true
        case _ => false
      }
    }
  }

  property("collect") = forAll { (n: Int, m: Int) =>
    (int.collect {
      case x if x < m => x
    }).parseOnly(n.toString).option match {
      case Some(`n`) if n < m => true
      case None if n >= m     => true
      case _ => false
    }
  }

  property("cons") = forAll { (c: Char, s: String) =>
    lazy val p: Parser[NonEmptyList[Char]] = cons(anyChar, orElse(p.map(_.toList), ok(List.empty[Char])))
    p.parseOnly(c + s).option == Some(NonEmptyList(c, s.toList))
  }

  // phrase

  property("many") = forAll { (s: String) =>
    many(anyChar).parseOnly(s).option == Some(s.toList)
  }

  property("many1") = forAll { (s: String) =>
    many1(anyChar).parseOnly(s).option == s.toList.toNel
      // Some(s.toList).filterNot(_.isEmpty)
  }

  property("manyN") = forAll { (s: String, n0: Int) =>
    val n = if (s.isEmpty) 0 else (n0.abs % s.length)
    manyN(n, anyChar).parseOnly(s).option ==
      Some(s.take(n).toList)
  }

  property("manyUntil") = forAll { (s: String) =>
    (s.nonEmpty && s.indexOf("x") == -1) ==> {
      val r = manyUntil(string(s), char('x')).parseOnly(s * 3 + "xyz").done
      r == ParseResult.Done("yz", List(s, s, s))
    }
  }

  property("skipMany") = forAll { (s: String) =>
    skipMany(anyChar).parseOnly(s).option == Option(s.toList).void
  }

  property("skipMany1") = forAll { (s: String) =>
    skipMany1(anyChar).parseOnly(s).option ==
      Option(s.toList).filterNot(_.isEmpty).void
  }

  property("skipManyN") = forAll { (s: String, n0: Int) =>
    val n = if (s.isEmpty) 0 else (n0.abs % s.length)
    skipManyN(n, anyChar).parseOnly(s).option ==
      Option(s.take(n).toList).void
  }

  property("sepBy") = forAll { (ns: List[Int], c: Char, s: String) =>
    val sep = s + c
    !(sep.exists(_.isDigit)) ==> {
      val p = sepBy(int, string(sep))
      p.parseOnly(ns.mkString(sep)) match {
        case ParseResult.Done("", `ns`) => true
        case _ => false
      }
    }
  }

  property("sepBy1") = forAll { (n0: Int, ns0: List[Int], c: Char, s: String) =>
    val ns = NonEmptyList(n0, ns0)
    val sep = s + c
    !(sep.exists(_.isDigit)) ==> {
      val p = sepBy1(int, string(sep))
      p.parseOnly(ns.toList.mkString(sep)) match {
        case ParseResult.Done("", `ns`) => true
        case _ => false
      }
    }
  }

  property("choice/1") = forAll { (a: String, b: String, c: String) =>
    (a.nonEmpty && b.nonEmpty && c.nonEmpty) ==> {
      val p = choice(string(a), string(b), string(c))
      p.parseOnly(a + b + c).done == ParseResult.Done(b + c, a)
      p.parseOnly(b + c + a).done == ParseResult.Done(c + a, b)
      p.parseOnly(c + a + b).done == ParseResult.Done(a + b, c)
    }
  }

  property("choice/2") = forAll { (a: String, b: String, c: String) =>
   (a.nonEmpty && b.nonEmpty && c.nonEmpty) ==> {
      val p = choice(string(a), string(b), string(c))
      p.parseOnly(a + b + c).done == ParseResult.Done(b + c, a)
      p.parseOnly(b + c + a).done == ParseResult.Done(c + a, b)
      p.parseOnly(c + a + b).done == ParseResult.Done(a + b, c)
    }
  }

  property("opt") = forAll { (c: Char) =>
    opt(letterOrDigit).parseOnly(c.toString).option == Some(Some(c).filter(_.isLetterOrDigit))
  }

  property("filter") = forAll { (c: Char) =>
    filter(anyChar)(_.isLetterOrDigit).parseOnly(c.toString).option ==
      Some(c).filter(_.isLetterOrDigit)
  }

  property("count") = forAll { (c: Char, n0: Int) =>
    val n = (n0.abs % 10) + 1
    val s = c.toString * 20
    count(n, char(c)).parseOnly(s) == ParseResult.Done(s drop n, List.fill(n)(c))
  }

}
