package attoparsec

import scalaz._
import Scalaz._

/** Combinators. */
trait Combinators {
  
  import Free.Trampoline
  import Trampoline._
  import Parser.State
  import Parser.Success
  import Parser.Failure
  import Parser.Internal._

  /** Parser that consumes no data and produces the specified value. */
  def ok[A](a: A): Parser[A] = 
    new Parser[A] { 
      override def toString = "ok(" + a + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]] = 
        suspend(ks(st0,a))
    }

  /** Parser that consumes no data and fails with the specified error. */
  def err(what: String): Parser[Nothing] = 
    new Parser[Nothing] {
      override def toString = "err(" + what + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Nothing,R]): Trampoline[Result[R]] = 
        suspend(kf(st0,Nil, "Failed reading: " + what))
    }

  //////

  private def prompt[R](st0: State, kf: State => Trampoline[Result[R]], ks: State => Trampoline[Result[R]]): Trampoline[Result[R]] = 
    done(Partial[R](s => 
      if (s == "") kf(st0 copy (complete = true)).run
      else ks(st0 + s).run
    ))

  def demandInput: Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "demandInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Trampoline[Result[R]] = 
        if (st0.complete)
          suspend(kf(st0,List("demandInput"),"not enough bytes"))
        else
          suspend(prompt(st0, st => kf(st,List("demandInput"),"not enough bytes"), a => ks(a,())))
    }

  def ensure(n: Int): Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "ensure(" + n + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Trampoline[Result[R]] = 
        if (st0.input.length >= n)
          suspend(ks(st0,()))
        else
          suspend((demandInput ~> ensure(n))(st0,kf,ks))
    }

  val wantInput: Parser[Boolean] = 
    new Parser[Boolean] {
      override def toString = "wantInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Boolean,R]): Trampoline[Result[R]] = 
        suspend(if (st0.input != "")   ks(st0,true)
        else if (st0.complete) ks(st0,false)
        else prompt(st0, a => ks(a,false), a => ks(a,true)))
    }

  //////

  /** Parser that produces the remaining input (but does not consume it). */
  val get: Parser[String] = 
    new Parser[String] {
      override def toString = "get"
      def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): Trampoline[Result[R]] = 
        suspend(ks(st0,st0.input))
    }

  /** Parser that replaces the remaining input and produces (). */
  def put(s: String): Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "put(" + s + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Trampoline[Result[R]] = 
        suspend(ks(st0 copy (input = s), ()))
    }

  //////

  // attoparsec try
  def attempt[T](p: Parser[T]): Parser[T] = 
    new Parser[T] { 
      override def toString = "attempt(" + p + ")"
      def apply[R](st0: State, kf: Failure[R], ks: Success[T,R]): Trampoline[Result[R]] = 
        suspend(p(st0.noAdds, (st1: State, stack: List[String], msg: String) => kf(st0 + st1, stack, msg), ks))
    }
  

  /** Parser that matches end of input. */
  val endOfInput: Parser[Unit] = 
    new Parser[Unit] {
      override def toString = "endOfInput"
      def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Trampoline[Result[R]] = 
        suspend(if (st0.input == "") {
          if (st0.complete)
            ks(st0,())
          else 
            demandInput(
              st0,
              (st1: State, stack: List[String], msg: String) => ks(st0 + st1,()), 
              (st1: State, u: Unit) => kf(st0 + st1, Nil, "endOfInput")
            )
        } else kf(st0,Nil,"endOfInput"))
    }

  /** Parser that matches `p` only if there is no remaining input */
  def phrase[A](p: Parser[A]): Parser[A] = 
    p <~ endOfInput as ("phrase" + p)

  // TODO: return a parser of a reducer of A
  /** Parser that matches zero or more `p`. */
  def many[A](p: => Parser[A]): Parser[List[A]] = {
    lazy val many_p : Parser[List[A]] = (p cons many_p) | ok(Nil)
    many_p as ("many(" + p + ")")
  }

  /** Parser that matches one or more `p`. */
  def many1[A](p: => Parser[A]): Parser[List[A]] = 
    p cons many(p)

  def manyTill[A](p: Parser[A], q: Parser[Any]): Parser[List[A]] = { 
    lazy val scan : Parser[List[A]] = (q ~> ok(Nil)) | (p cons scan) 
    scan as ("manyTill(" + p + "," + q + ")")
  }

  def skipMany1(p: Parser[Any]): Parser[Unit] = 
    (p ~> skipMany(p)) as ("skipMany1(" + p + ")")

  def skipMany(p: Parser[Any]): Parser[Unit] = { 
    lazy val scan : Parser[Unit] = (p ~> scan) | ok(()) 
    scan as ("skipMany(" + p + ")")
  }
      
  def sepBy[A](p: Parser[A], s: Parser[Any]): Parser[List[A]] =
    (p cons ((s ~> sepBy1(p,s)) | ok(Nil))) | ok(Nil) as ("sepBy(" + p + "," + s + ")")

  def sepBy1[A](p: Parser[A], s: Parser[Any]): Parser[List[A]] = {
    lazy val scan : Parser[List[A]] = (p cons (s ~> scan)) | ok(Nil)
    scan as ("sepBy1(" + p + "," + s + ")")
  }

  def choice[A](xs : Parser[A]*) : Parser[A] = 
    ((err("choice") : Parser[A]) /: xs)(_ | _) as ("choice(" + xs.toString + " :_*)")

  def choice[A](xs : TraversableOnce[Parser[A]]) : Parser[A] = 
    ((err("choice") : Parser[A]) /: xs)(_ | _) as ("choice(...)")

  def opt[A](m: Parser[A]): Parser[Option[A]] = 
    (attempt(m).map(some(_)) | ok(none)) as ("opt(" + m + ")")

}
