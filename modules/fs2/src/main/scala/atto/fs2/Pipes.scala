package atto.fs2

import atto._
import atto.Atto._
import atto.ParseResult._

import java.lang.String
import scala.{Some, None, Unit}
// import scala.{ List, Nil }
import scala.language._

import _root_.fs2._

object Pipes {

  /** Parse a stream and return a single terminal ParseResult. */
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


}