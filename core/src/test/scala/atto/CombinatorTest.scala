package atto
import Atto._

import org.scalacheck._
import scalaz._

object CombinatorTest extends Properties("Combinator") {
  import Prop._
  import Parser._

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

  // many

  // many1

  // manyN

  // manyTill

  // skipMany1

  // skipMany

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

  // choice (1)

  // choice (2)

  // opt

  // filter

  // count

}

