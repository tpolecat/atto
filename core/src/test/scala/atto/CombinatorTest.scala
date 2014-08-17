package atto
import Atto._

import org.scalacheck._
import scalaz._
import scalaz.syntax.functor._
import scalaz.std.option._
import scalaz.std.anyVal.{ char => charInstance }

object CombinatorTest extends Properties("Combinator") {
  import Prop._
  import Parser._

  implicit val eqChar: Equal[Char] = Order[Char] // :-\

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

  property("put") = forAll { (s: String, x: String) => 
    put(x).parseOnly(s) match {
      case ParseResult.Done(`x`, ()) => true
      case _ => false
    }
  }

  // TODO: demandInput

  property("ensure") = forAll { (s: String, n: Int) =>
    ensure(n).parseOnly(s) match {
      case ParseResult.Done(s, ()) if n <= s.length => true
      case ParseResult.Fail(_, _, _) => true
      case _ => false
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

  property("andThen") = forAll { (n: Int, c: Char, s: String) =>
    !(s.exists(_.isDigit)) ==> { 
      andThen(string(s), int).parseOnly(s + n) match {
        case ParseResult.Done("", (`s`, `n`)) => true
        case _ => false
      }
    }    
  }

  property("orElse/left") = forAll { (n: Int, s: String) => 
    !(s.isEmpty || s.exists(_.isDigit)) ==> { 
      val p1: Parser[Int \/ String] = string(s).map(\/-(_))
      val p2: Parser[Int \/ String] = int.map(-\/(_))
      orElse(p1, p2).parseOnly(n + s) match {
        case ParseResult.Done(`s`, -\/(`n`)) => true
        case _ => false
      }
    }
  }

  property("orElse/right") = forAll { (n: Int, s: String) => 
    !(s.exists(_.isDigit)) ==> { 
      val p1: Parser[Int \/ String] = string(s).map(\/-(_))
      val p2: Parser[Int \/ String] = int.map(-\/(_))
      orElse(p1, p2).parseOnly(s + n) match {
        case ParseResult.Done(_, \/-(`s`)) => true
        case _ => false
      }
    }
  }

  property("either/left") = forAll { (n: Int, s: String) => 
    !(s.isEmpty || s.exists(_.isDigit)) ==> { 
      either(int, string(s)).parseOnly(n + s) match {
        case ParseResult.Done(`s`, -\/(`n`)) => true
        case _ => false
      }
    }
  }

  property("either/right") = forAll { (n: Int, s: String) => 
    !(s.isEmpty || s.exists(_.isDigit)) ==> { 
      either(int, string(s)).parseOnly(s + n) match {
        case ParseResult.Done(_, \/-(`s`)) => true
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
    lazy val p: Parser[List[Char]] = cons(anyChar, orElse(p, ok(Nil)))
    p.parseOnly(c + s).option == Some(c :: s.toList)
  }  

  // phrase

  property("many") = forAll { (s: String) =>
    many(anyChar).parseOnly(s).option == Some(s.toList)
  }

  property("many1") = forAll { (s: String) =>
    many1(anyChar).parseOnly(s).option == 
      Some(s.toList).filterNot(_.isEmpty)
  }

  property("manyN") = forAll { (s: String, n0: Int) =>
    val n = n0.abs
    (n >= 0) ==> {
      manyN(n, anyChar).parseOnly(s).option match {
        case None => s.length < n
        case Some(chars) => chars == s.take(n).toList
      }
    }
  }


  property("upToN") = forAll { (s: String, n0: Int) =>
    val n = n0.abs
    (n >= 0) ==> {
      val parseLen = n min s.length
      val left = s.takeRight(s.length - parseLen)
      upToN(n, anyChar).parseOnly(s) match {
        case res@ParseResult.Done(x, y) =>
          (y.length == parseLen) :| s"parsed ${y.length} characters instead of $parseLen" &&
            (x == left) :| s"remainder expected '$left', got '$x' in $res"
        case _ => false :| "parse failed"
      }
    }
  }

  property("manyNM") = forAll { (s: String, n0: Int, m0: Int) =>
    val n = n0.abs
    val m = n + m0.abs
    (n >= 0 && m >= n) ==> {
      val result = manyNM(n, m, anyChar).parseOnly(s)
      if (s.length >= n) {
        val parseLen = m min s.length
        val left = s.takeRight(if (s.length > m) s.length - m else 0)
         result match {
          case ParseResult.Done(x, y) =>
            (parseLen == y.length) :| s"expected $parseLen characters but got ${y.length}" &&
              (x == left) :| s"remainder expected '$left', got '$x' in $result"
          case _ => (s.length < n) :| "parse failed"
        }
      } else // s.length < n
        result match {
        case ParseResult.Fail(x, y, _) =>
          (x == s) :| "shouldn't consume any input"
        case _ => false
      }
    }
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

  property("skipManyN") = forAll { (s: String, n: Int) =>
    (n >= 0) ==> {
      (skipManyN(n, anyChar).parseOnly(s).option
        == (if (s.length < n) None else Some(())))
    }

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

  property("sepBy1") = forAll { (ns: List[Int], c: Char, s: String) =>
    val sep = s + c
    !(sep.exists(_.isDigit)) ==> { 
      val p = sepBy1(int, string(sep))
      p.parseOnly(ns.mkString(sep)) match {
        case ParseResult.Fail(s, _, _) if ns.isEmpty => true
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

