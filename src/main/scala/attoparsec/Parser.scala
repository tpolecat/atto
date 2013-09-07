package attoparsec

import scala.language.implicitConversions
import scala.language.higherKinds

import scalaz._
import scalaz.Scalaz._
import Free.Trampoline

abstract class Parser[+A] { m => 
  import Parser._
  import Parser.Internal._
  import Trampoline._

  def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]]
  
  def infix(s: String): String = 
    "(" + m.toString + ") " + s

  def flatMap[B](f: A => Parser[B]): Parser[B] = 
    new Parser[B] {
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Trampoline[Result[R]] = 
        suspend(m(st0,kf,(s:State, a:A) => f(a)(s,kf,ks)))
      override def toString = m infix "flatMap ..."
    }

  def map[B](f: A => B): Parser[B] = 
    new Parser[B] { 
      override def toString = m infix "map ..."
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Trampoline[Result[R]] =
        suspend(m(st0,kf,(s:State, a:A) => suspend(ks(s,f(a)))))
    }

  class WithFilter(p: A => Boolean) extends Parser[A] { 
    override def toString = m infix "withFilter ..."
    def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]] =
      suspend(m(st0,kf,(s:State, a:A) => if (p(a)) ks(s,a) else kf(s, Nil, "withFilter")))
    override def map[B](f: A => B) = m filter p map f
    override def flatMap[B](f: A => Parser[B]) = m filter p flatMap f
    override def withFilter(q: A => Boolean): WithFilter =
      new WithFilter(x => p(x) && q(x))
    override def filter(q: A => Boolean): WithFilter = 
      new WithFilter(x => p(x) && q(x))
  }

  def withFilter(p: A => Boolean): WithFilter = 
    new WithFilter(p)

  def filter(p: A => Boolean): Parser[A] = 
    new WithFilter(p)

  final def ~>[B](n: Parser[B]): Parser[B] = 
    new Parser[B] { 
      override def toString = m infix ("~> " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Trampoline[Result[R]] =
        suspend(m(st0,kf,(s:State, a: A) => n(s, kf, ks)))
    }

  final def <~[B](n: Parser[B]): Parser[A] = 
    new Parser[A] {
      override def toString = m infix ("<~ " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]] =
        suspend(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, b: B) => ks(st2, a))))
    }
  
  final def ~[B](n: Parser[B]): Parser[(A,B)] = 
    new Parser[(A,B)] { 
      override def toString = m infix ("~ " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[(A,B),R]): Trampoline[Result[R]] =
        suspend(m(st0,kf,(st1:State, a: A) => n(st1, kf, (st2: State, b: B) => ks(st2, (a, b)))))
    }

  final def |[B >: A](n: => Parser[B]): Parser[B] = 
    new Parser[B] {
      override def toString = m infix ("| ...")
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): Trampoline[Result[R]] =         
        suspend(m(st0.noAdds, (st1: State, stack: List[String], msg: String) => n(st0 + st1, kf, ks), ks))
    }

  final def cons[B >: A](n: => Parser[List[B]]): Parser[List[B]] = 
    m flatMap (x => n map (xs => x :: xs))

  final def ||[B >: A](n: => Parser[B]): Parser[Either[A,B]] = 
    new Parser[Either[A,B]] { 
      override def toString = m infix ("|| " + n)
      def apply[R](st0: State, kf: Failure[R], ks: Success[Either[A,B],R]): Trampoline[Result[R]] = 
        suspend(m(
          st0.noAdds, 
          (st1: State, stack: List[String], msg: String) => n (st0 + st1, kf, (st1: State, b: B) => ks(st1, Right(b))), 
          (st1: State, a: A) => ks(st1, Left(a))
        ))
    }

  final def matching[B](f: PartialFunction[A,B]): Parser[B] = 
    m.filter(f isDefinedAt _).map(f)

  // final def ? : Parser[Option[A]] = opt(m)
  // final def + : Parser[List[A]] = many1(m)
  // final def * : Parser[List[A]] = many(m)

  // final def *(s: Parser[Any]): Parser[List[A]] = sepBy(m,s)
  // final def +(s: Parser[Any]): Parser[List[A]] = sepBy1(m,s) 

  final def parse(b: String): ParseResult[A] = 
    m(State(b, "", false), (a,b,c) => done(Fail(a, b, c)), (a,b) => done(Done(a, b))).run.translate

  final def parseOnly(b: String): ParseResult[A] = 
    m(State(b, "", true),  (a,b,c) => done(Fail(a, b, c)), (a,b) => done(Done(a, b))).run.translate

  final def as(s: => String): Parser[A] = // attoparsec <?>
    new Parser[A] { 
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]] = 
        suspend(m(st0, (st1: State, stack: List[String], msg: String) => kf(st1, s :: stack, msg), ks))
    }

  final def asOpaque(s: => String): Parser[A] = 
    new Parser[A] { 
      override def toString = s
      def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): Trampoline[Result[R]] = 
        suspend(m(st0, (st1: State, stack: List[String], msg: String) => kf(st1, Nil, "Failure reading:" + s), ks))
    }

}

sealed abstract class ParseResult[+A] { 
  def map[B](f: A => B): ParseResult[B]
  def feed(s: String): ParseResult[A]
  def option: Option[A]
  def either: String \/ A
  def done: ParseResult[A] = feed("")
}

object ParseResult { 

  case class Fail(input: String, stack: List[String], message: String) extends ParseResult[Nothing] { 
    def map[B](f: Nothing => B) = Fail(input, stack, message)
    def feed(s: String) = this
    override def done = this
    def option = None
    def either = -\/(message)
  }

  case class Partial[+T](k: String => ParseResult[T]) extends ParseResult[T] { 
    def map[B](f: T => B) = Partial(s => k(s).map(f))
    def feed(s: String) = k(s)
    def option = None
    def either = -\/("incomplete input")
  }

  case class Done[+T](input: String, result: T) extends ParseResult[T] { 
    def map[B](f: T => B) = Done(input, f(result))
    def feed(s: String) = Done(input + s, result)
    override def done = this
    def option = Some(result)
    def either = \/-(result)
  }

  implicit def show[A] = Show.showA[ParseResult[A]]


  // RCN: not sure about these...

  // implicit def translate[T](r: Parser.Internal.Result[T]) : ParseResult[T] = r.translate
  // implicit def option[T](r: ParseResult[T]): Option[T] = r.option
  // implicit def either[T](r: ParseResult[T]): Either[String,T] = r.either

}

object Parser extends ParserInstances with Text { 

  object Internal { 
    sealed abstract class Result[+T] { 
      def translate: ParseResult[T]
    }
    case class Fail(input: State, stack: List[String], message: String) extends Result[Nothing] { 
      def translate = ParseResult.Fail(input.input, stack, message)
      def push(s: String) = Fail(input, stack = s :: stack, message)
    }
    case class Partial[+T](k: String => Trampoline[Result[T]]) extends Result[T] { 
      def translate = ParseResult.Partial(a => k(a).run.translate)
    }
    case class Done[+T](input: State, result: T) extends Result[T] { 
      def translate = ParseResult.Done(input.input, result)
    }
  }

  import Internal._

  case class State(input: String, added: String, complete: Boolean) {
    def +(rhs: State) = State(input + rhs.added, added + rhs.added, complete | rhs.complete)
    def +(rhs: String) = State(input + rhs, added + rhs, complete)
    def completed: State = copy(complete = true)
    def noAdds: State = copy(added = "")
  }

  type Failure[+R] = (State,List[String],String) => Trampoline[Result[R]]
  type Success[-A,+R] = (State, A) => Trampoline[Result[R]]

  // def parse[A](m: Parser[A], init: String): ParseResult[A] = 
  //   m parse init
  
  // def parse[M[_]:Monad, A](m: Parser[A], refill: M[String], init: String): M[ParseResult[A]] = {
  //   def step[A] (r: Result[A]): M[ParseResult[A]] = r match {
  //     case Partial(k) => refill flatMap (a => step(k(a)))
  //     case x => x.translate.pure[M]
  //   }
  //   step(m(State(init, "", false),(a,b,c) => done(Fail(a, b, c)), (a,b) => done(Done(a, b))))
  // }

  // def parseAll[A](m: Parser[A], init: String): ParseResult[A] = 
  //   Parser.phrase(m) parse init

  // def parseAll[M[_]:Monad, A](m: Parser[A], refill: M[String], init: String): M[ParseResult[A]] =
  //   parse[M,A](Parser.phrase(m), refill, init)

}

trait ParserInstances {

  implicit def monad: Monad[Parser] = 
    new Monad[Parser] { 
      def point[A](a: => A): Parser[A] = Parser.ok(a)
      def bind[A,B](ma: Parser[A])(f: A => Parser[B]) = ma flatMap f
    }

  implicit def plus: Plus[Parser] = 
    new Plus[Parser] { 
      def plus[A](a: Parser[A], b: => Parser[A]): Parser[A] = a | b
    }

  implicit def monoid[A]: Monoid[Parser[A]] = 
    new Monoid[Parser[A]] { 
      def append(s1: Parser[A], s2: => Parser[A]): Parser[A] = s1 | s2
      val zero: Parser[A] = Parser.err("zero")
    }

}





