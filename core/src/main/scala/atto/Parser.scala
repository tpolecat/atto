package atto

import scala.language.implicitConversions
import scala.language.higherKinds

import scalaz._
import Scalaz._
import Free.Trampoline
import Trampoline._

// Operators not needed for use in `for` comprehensions are provided via added syntax.
trait Parser[+A] { m => 
  import Parser._
  import Parser.Internal._

  def apply[R](st0: State, kf: Failure[R], ks: Success[A,R]): TResult[R]
  
  // TODO: get rid of this
  def infix(s: String): String = 
    "(" + m.toString + ") " + s

  def flatMap[B](f: A => Parser[B]): Parser[B] = 
    new Parser[B] {
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] = 
        suspend(m(st0,kf,(s:State, a:A) => f(a)(s,kf,ks)))
      override def toString = m infix "flatMap ..."
    }

  def map[B](f: A => B): Parser[B] = 
    new Parser[B] { 
      override def toString = m infix "map ..."
      def apply[R](st0: State, kf: Failure[R], ks: Success[B,R]): TResult[R] =
        suspend(m(st0,kf,(s:State, a:A) => suspend(ks(s,f(a)))))
    }

  def filter(p: A => Boolean): Parser[A] = 
    parser.combinator.filter(this, p)

}

object Parser extends ParserInstances with ParserFunctions { 

  case class State(input: String, added: String, complete: Boolean) {
    def +(rhs: State) = State(input + rhs.added, added + rhs.added, complete | rhs.complete)
    def +(rhs: String) = State(input + rhs, added + rhs, complete)
    def completed: State = copy(complete = true)
    def noAdds: State = copy(added = "")
  }

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

  type TResult[+R] = Trampoline[Result[R]]
  type Failure[+R] = (State,List[String],String) => TResult[R]
  type Success[-A,+R] = (State, A) => TResult[R]

}

trait ParserFunctions {
  import Parser._
  import Parser.Internal._

  def parse[A](m: Parser[A], b: String): ParseResult[A] = 
    m(State(b, "", false), (a,b,c) => done(Fail(a, b, c)), (a,b) => done(Done(a, b))).run.translate

  def parseOnly[A](m: Parser[A], b: String): ParseResult[A] = 
    m(State(b, "", true),  (a,b,c) => done(Fail(a, b, c)), (a,b) => done(Done(a, b))).run.translate
  
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
  import parser.combinator._
  import syntax.parser._

  implicit def monad: Monad[Parser] = 
    new Monad[Parser] { 
      def point[A](a: => A): Parser[A] = ok(a)
      def bind[A,B](ma: Parser[A])(f: A => Parser[B]) = ma flatMap f
    }

  implicit def plus: Plus[Parser] = 
    new Plus[Parser] { 
      def plus[A](a: Parser[A], b: => Parser[A]): Parser[A] = a | b
    }

  implicit def monoid[A]: Monoid[Parser[A]] = 
    new Monoid[Parser[A]] { 
      def append(s1: Parser[A], s2: => Parser[A]): Parser[A] = s1 | s2
      val zero: Parser[A] = err("zero")
    }

}





