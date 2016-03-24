package atto

import atto.compat.Eithery
import java.lang.String
import scala.{ Option, Some, None, Nothing, List }
import scala.language.higherKinds

sealed abstract class ParseResult[A] {
  def map[B](f: A => B): ParseResult[B]
  def feed(s: String): ParseResult[A]
  def option: Option[A]
  def either[E[_, _]: Eithery]: E[String, A]
  def done: ParseResult[A] = feed("")
}

object ParseResult {

  case class Fail[A](input: String, stack: List[String], message: String) extends ParseResult[A] {
    def map[B](f: A => B) = Fail(input, stack, message)
    def feed(s: String) = this
    override def done = this
    def option = None
    def either[E[_, _]](implicit E: Eithery[E]): E[String, A] = E.left(message)
  }

  case class Partial[A](k: String => ParseResult[A]) extends ParseResult[A] {
    def map[B](f: A => B) = Partial(s => k(s).map(f))
    def feed(s: String) = k(s)
    def option = None
    def either[E[_, _]](implicit E: Eithery[E]): E[String, A] = E.left("incomplete input")
  }

  case class Done[A](input: String, result: A) extends ParseResult[A] {
    def map[B](f: A => B) = Done(input, f(result))
    def feed(s: String) = Done(input + s, result)
    override def done = this
    def option = Some(result)
    def either[E[_, _]](implicit E: Eithery[E]): E[String, A] = E.right(result)
  }

}

