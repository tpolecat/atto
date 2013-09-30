package atto

import scalaz._
import Scalaz._

sealed abstract class ParseResult[+A] { 
  def map[B](f: A => B): ParseResult[B]
  def feed(s: String): ParseResult[A]
  def option: Option[A]
  def either: String \/ A
  def done: ParseResult[A] = feed("")
}

object ParseResult extends ParseResultInstances { 

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

}

trait ParseResultInstances {

  implicit val functor: Functor[ParseResult] =
    new Functor[ParseResult] {
      def map[A,B](ma: ParseResult[A])(f: A => B) = 
        ma map f
    }

}

