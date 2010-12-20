package attoparsec

import scalaz._
import scalaz.Scalaz._

abstract class Parser[+A] { m => 
  import Parser._
  import Parser.Internal._
  def apply[R](st0: State, kf:Failure[R], ks: Success[A,R]): Result[R]

  final def flatMap[B](f: A => Parser[B]): Parser[B] = new Parser[B] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Result[R] = 
      m(st0,kf,(s:State, a:A) => f(a)(s,kf,ks))
  }
  final def map[B](f: A => B): Parser[B] = new Parser[B] { 
    def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Result[R] =
      m(st0,kf,(s:State, a:A) => ks(s,f(a)))
  }
  final def filter(p: A => Boolean): Parser[A] = m flatMap (a => 
    if (p(a)) ok(a)
    else err("filter")
  )
  final def ~> [B](n: Parser[B]): Parser[B] = m flatMap (_ => n)
  final def <~ [B](n: Parser[B]): Parser[A] = m flatMap (a => n map (_ => a))
  final def ~ [B](n: Parser[B]): Parser[(A,B)] = m flatMap (a => n map (b => (a,b)))
  final def | [B >: A](n: Parser[B]): Parser[B] = new Parser[B] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Result[R] = 
      m(st0.noAdds, (st1: State, stack: List[String], msg: String) => n(st0 + st1, kf, ks), ks)
  }
  final def cons [B >: A](n: Parser[List[B]]): Parser[List[B]] = m flatMap (x => n map (xs => x :: xs))
  final def || [B >: A](n: Parser[B]): Parser[Either[A,B]] = new Parser[Either[A,B]] { 
    def apply[R](st0: State, kf: Failure[R], ks: Success[Either[A,B],R]): Result[R] = 
      m(
        st0.noAdds, 
        (st1: State, stack: List[String], msg: String) => n (st0 + st1, kf, (st1: State, b: B) => ks(st1, Right(b))), 
        (st1: State, a: A) => ks(st1, Left(a))
      )
  }
  final def matching[B](f: PartialFunction[A,B]): Parser[B] = m flatMap (a => 
    if (f isDefinedAt a) ok(f(a))
    else err("partial function")
  )

  final def ? : Parser[Option[A]] = opt(m)
  final def + : Parser[List[A]] = many1(m)
  final def * : Parser[List[A]] = many(m)

  final def *(s: Parser[Any]): Parser[List[A]] = sepBy(m,s)
  final def +(s: Parser[Any]): Parser[List[A]] = sepBy1(m,s) 

  final def parse(b: String): ParseResult[A] = m(b, Fail(_,_,_), Done(_,_)).translate


  final def as(s: String): Parser[A] = m | err(s)
}

trait ParseResult[+A] { 
  def map[B](f: A => B): ParseResult[B]
  def feed(s: String): ParseResult[A]
  def option: Option[A]
  def either: Either[String,A]
}
object ParseResult { 
  case class Fail(input: String, stack: List[String], message: String) extends ParseResult[Nothing] { 
    def map[B](f: Nothing => B) = Fail(input, stack, message)
    def feed(s: String) = this
    def option = None
    def either = Left(message)
  }
  case class Partial[+T](k: String => ParseResult[T]) extends ParseResult[T] { 
    def map[B](f: T => B) = Partial(s => k(s).map(f))
    def feed(s: String) = k(s)
    def option = None
    def either = Left("incomplete input")
  }
  case class Done[+T](input: String, result: T) extends ParseResult[T] { 
    def map[B](f: T => B) = Done(input, f(result))
    def feed(s: String) = Done(input + s, result)
    def option = Some(result)
    def either = Right(result)
  }

  implicit def translate[T](r: Parser.Internal.Result[T]) : ParseResult[T] = r.translate
  implicit def option[T](r: ParseResult[T]): Option[T] = r.option
  implicit def either[T](r: ParseResult[T]): Either[String,T] = r.either
}

object Parser { 
  object Internal { 
    trait Result[+T] { 
      def translate: ParseResult[T]
    }
    case class Fail(input: State, stack: List[String], message: String) extends Result[Nothing] { 
      def translate = ParseResult.Fail(input.input, stack, message)
    }
    case class Partial[+T](k: String => Result[T]) extends Result[T] { 
      def translate = ParseResult.Partial(a => k(a).translate)
    }
    case class Done[+T](input: State, result: T) extends Result[T] { 
      def translate = ParseResult.Done(input.input, result)
    }
  }
  import Internal._

  case class State(input: String, added: String, complete: Boolean) {
    def +(rhs: State) = State(input + rhs.added, added + rhs.added, complete | rhs.complete)
    def +(rhs: String) = State(input + rhs, added + rhs, complete)
    def completed: State = copy (complete = true)
    def noAdds: State = copy (added = "")
  }

  implicit def stateString(s: State): String = s.input
  implicit def stringState(s: String): State = State(s, "", false)

  type Failure[+R] = (State,List[String],String) => Result[R]
  type Success[-A,+R] = (State, A) => Result[R]

  def ok[A](a: A): Parser[A] = new Parser[A] { 
    def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Result[R] = 
      ks(st0,a)
  }

  def err(what: String): Parser[Nothing] = new Parser[Nothing] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Nothing,R]): Result[R] = 
      kf(st0,Nil, "Failed reading: " ++ what)
  }

  def prompt[R](st0: State, kf: State => Result[R], ks: State => Result[R]) = Partial[R](s => 
    if (s == "") kf(st0 copy (complete = true))
    else ks(st0 + s)
  )

  def demandInput: Parser[Unit] = new Parser[Unit] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Result[R] = 
      if (st0.complete)
        kf(st0,List("demandInput"),"not enough bytes")
      else
        prompt(st0, st => kf(st,List("demandInput"),"not enough bytes"), a => ks(a,()))
  }

  def ensure(n: Int): Parser[Unit] = new Parser[Unit] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Result[R] = 
      if (st0.input.length >= n)
        ks(st0,())
      else
        (demandInput ~> ensure(n))(st0,kf,ks)
  }

  def wantInput: Parser[Boolean] = new Parser[Boolean] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Boolean,R]): Result[R] = 
      if (st0.input != "")   ks(st0,true)
      else if (st0.complete) ks(st0,false)
      else prompt(st0, a => ks(a,false), a => ks(a,true))
  }

  def get: Parser[String] = new Parser[String] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[String,R]): Result[R] = 
      ks(st0,st0.input)
  }

  def put(s: String): Parser[Unit] = new Parser[Unit] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Result[R] = 
      ks(st0 copy (input = s), ())
  }

  // attoparsec try
  def attempt[T](p: Parser[T]): Parser[T] = new Parser[T] { 
    def apply[R](st0: State, kf: Failure[R], ks: Success[T,R]): Result[R] = 
      p(st0.noAdds, (st1: State, stack: List[String], msg: String) => kf(st0 + st1, stack, msg), ks)
  }
  
  def elem(p: Char => Boolean): Parser[Char] = 
    ensure(1) ~> get flatMap (s => {
      val c = s.charAt(0)
      if (p(c)) put(s.substring(1)) ~> ok(c)
      else err("elem")
    })

  def skip(p: Char => Boolean): Parser[Unit] = 
    ensure(1) ~> get flatMap (s => {
      if (p(s.charAt(0))) put(s.substring(1))
      else err("skip")
    })

  def takeWith(n: Int, p: String => Boolean): Parser[String] =
    ensure(n) ~> get flatMap (s => {
      val w = s.substring(0,n)
      if (p(w)) put(s.substring(n)) ~> ok(w)
      else err("takeWith")
    })

  def take(n: Int): Parser[String] = takeWith(n, _ => true)

  implicit def char(c: Char): Parser[Char] = elem(_==c).as(c.toString)
  implicit def string(s: String): Parser[String] = takeWith(s.length, _ == s).as(s)

  def stringTransform(f: String => String, s: String): Parser[String] = 
    takeWith(s.length, f(_) == f(s))

  def endOfInput: Parser[Unit] = new Parser[Unit] {
    def apply[R](st0: State, kf: Failure[R], ks: Success[Unit,R]): Result[R] = 
      if (st0.input == "") {
        if (st0.complete)
          ks(st0,())
        else 
          demandInput(
            st0,
            (st1: State, stack: List[String], msg: String) => ks(st0 + st1,()), 
            (st1: State, u: Unit) => kf(st0 + st1,Nil,"endOfInput")
          )
      } else kf(st0,Nil,"endOfInput")
  }
  def phrase[A](p: Parser[A]): Parser[A] = p <~ endOfInput

  // TODO: return a parser of a reducer of A
  def many[A](p: Parser[A]): Parser[List[A]] = {
    lazy val many_p : Parser[List[A]] = (p cons many_p) | ok(Nil)
    many_p
  }

  def many1[A](p: Parser[A]): Parser[List[A]] = p cons many(p)

  def manyTill[A](p: Parser[A], q: Parser[Any]): Parser[List[A]] = { 
    lazy val scan : Parser[List[A]] = (q ~> ok(Nil)) | (p cons scan)
    scan
  }

  def skipMany1(p: Parser[Any]): Parser[Unit] = p ~> skipMany(p)

  def skipMany(p: Parser[Any]): Parser[Unit] = { 
    lazy val scan : Parser[Unit] = (p ~> scan) | ok(()) 
    scan 
  }
      
  def sepBy[A](p: Parser[A], s: Parser[Any]): Parser[List[A]] =
    (p cons ((s ~> sepBy1(p,s)) | ok(Nil))) | ok(Nil)

  def sepBy1[A](p: Parser[A], s: Parser[Any]): Parser[List[A]] = {
    lazy val scan : Parser[List[A]] = (p cons (s ~> scan)) | ok(Nil)
    scan
  }

  def choice[A](xs : TraversableOnce[Parser[A]]): Parser[A] =
    (err("choice").asInstanceOf[Parser[A]] /: xs)(_ | _)

  def opt[A](m: Parser[A]): Parser[Option[A]] = attempt(m).map(some(_)) | ok(none)

  def parse[A](m: Parser[A], init: String): ParseResult[A] = m parse init
  def parse[M[_]:Monad, A](m: Parser[A], refill: M[String], init: String): M[ParseResult[A]] = {
    def step[A] (r: Result[A]): M[ParseResult[A]] = r match {
      case Partial(k) => refill flatMap (a => step(k(a)))
      case x => x.translate.pure[M]
    }
    step(m(init,Fail(_,_,_),Done(_,_)))
  }

  def parseAll[A](m: Parser[A], init: String) = phrase(m) parse init
  def parseAll[M[_]:Monad, A](m: Parser[A], refill: M[String], init: String): M[ParseResult[A]] =
    parse[M,A](phrase(m), refill, init)
}
