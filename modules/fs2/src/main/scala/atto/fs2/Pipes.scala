package atto.fs2

import atto._
import atto.Atto._
import atto.ParseResult._

import java.lang.{String, SuppressWarnings}
import scala.{Some, None, Unit}
import scala.{ List, Nil, Array }
import scala.Predef.{ augmentString }
import scala.language._

import _root_.fs2._

object Pipes {

  /** Parse a stream and return a single terminal ParseResult. */
  @SuppressWarnings(Array("org.wartremover.warts.Recursion", "org.wartremover.warts.Any"))
  def parse1[F[_], A](p: Parser[A]): Pipe[F, String, ParseResult[A]] = s => {
    def go(r: ParseResult[A])(s: Stream[F, String]): Pull[F, ParseResult[A], Unit] = {
      r match {
        case Partial(_) => 
          s.pull.uncons1.flatMap{
            // Add String To Result If Stream Has More Values
            case Some((s, rest)) => go(r.feed(s))(rest)
            // Reached Stream Termination and Still Partial - Return the partial
            case None => Pull.output1(r)
          }
        case _ => Pull.output1(r)
      }
    }
    go(p.parse(""))(s).stream
  }

  /** Parse a stream into a series of values, halting on invalid input. */
  @SuppressWarnings(Array("org.wartremover.warts.Recursion", "org.wartremover.warts.Any"))
  def parseN[F[_], A](p: Parser[A]): Pipe[F, String, A] = s => {
    def exhaust(r: ParseResult[A], acc: List[A]): (ParseResult[A], List[A]) =
      r match {
        case Done(in, a) => exhaust(p.parse(in), a :: acc) 
        case _           => (r, acc)
      }

    def go(r: ParseResult[A])(s: Stream[F, String]): Pull[F, A, Unit] = {
      s.pull.uncons1.flatMap{
        case Some((s, rest)) =>
          val (r0, acc) = r match {
            case Done(in, a)    => (p.parse(in + s), List(a)) 
            case Fail(_, _, _) => (r, Nil)
            case Partial(_)     => (r.feed(s), Nil)
          }
          val (r1, as) = exhaust(r0, acc)
          Pull.output(Chunk.seq(as.reverse)) >> go(r1)(rest)
        case None => Pull.output(Chunk.seq(exhaust(r.done, Nil)._2))
      }
    }

    go(p.parse(""))(s).stream
  }

  /** Parse a stream into a series of values, discarding invalid input. */
  @SuppressWarnings(Array("org.wartremover.warts.Recursion", "org.wartremover.warts.Any"))
  def parseLenient[F[_], A](p: Parser[A]): Pipe[F, String, A] = s => {
    def exhaust(r: ParseResult[A], acc: List[A]): (ParseResult[A], List[A]) =
      r match {
        case Done(in, a)    => exhaust(p.parse(in), a :: acc) 
        case Fail(in, _, _) => exhaust(p.parse(in.drop(1)), acc)
        case Partial(_)     => (r, acc)
      }

    def go(r: ParseResult[A])(s: Stream[F, String]): Pull[F, A, Unit] = {
      s.pull.uncons1.flatMap{
        case Some((s, rest)) =>
          val (r0, acc) = r match {
            case Done(in, a)    => (p.parse(in + s), List(a)) 
            case Fail(in, _, _) => (p.parse(in.drop(1) + s), Nil)
            case Partial(_)     => (r.feed(s), Nil)
          }
          val (r1, as) = exhaust(r0, acc)
          Pull.output(Chunk.seq(as.reverse)) >> go(r1)(rest)
        case None => Pull.output(Chunk.seq(exhaust(r.done, Nil)._2))
      }
    }
    go(p.parse(""))(s).stream
  }

}