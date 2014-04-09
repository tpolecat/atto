package atto
package stream

import java.lang.String
import scala.{ List, Nil }
import scala.language._
import scala.Predef.{ augmentString }
import scalaz.{ Monad, Catchable, \/ }
import scalaz.syntax.monad._
import scalaz.stream._

import Atto._
import ParseResult._
import Process.{ await1, emit, emitAll, halt, suspend, await, eval }
import process1.{ id, scan }

object Stream extends StreamFunctions

trait StreamFunctions {

  /** Parse a stream and return a single terminal ParseResult. */
  def parse1[A](p: Parser[A]): Process1[String, ParseResult[A]] = {
    def go(r: ParseResult[A]): Process1[String, ParseResult[A]] = 
      r match {
        case Partial(_) => await1[String].flatMap(s => go(r feed s)) orElse emit(r.done)
        case _          => emit(r)
      }      
    go(p.parse(""))
  }

  /** Parse a stream into a series of values, halting on invalid input. */
  def parseN[A](p: Parser[A]): Process1[String, A] = {
    def exhaust(r: ParseResult[A], acc: List[A]): (ParseResult[A], List[A]) =
      r match {
        case Done(in, a) => exhaust(p.parse(in), a :: acc) 
        case _           => (r, acc)
      }
    def go(r: ParseResult[A]): Process1[String, A] =
      await1[String].flatMap { s =>
        val (r0, acc) = r match {
          case Done(in, a)    => (p.parse(in + s), List(a)) 
          case Fail(in, _, _) => (r, Nil)
          case Partial(_)     => (r.feed(s), Nil)
        }
        val (r1, as) = exhaust(r0, acc)
        emitAll(as.reverse) ++ go(r1)
      } orElse emitAll(exhaust(r.done, Nil)._2)
    go(p.parse(""))  
  }

  /** Parse a stream into a series of values, discarding invalid input. */
  def parseLenient[A](p: Parser[A]): Process1[String, A] = {
    def exhaust(r: ParseResult[A], acc: List[A]): (ParseResult[A], List[A]) =
      r match {
        case Done(in, a)    => exhaust(p.parse(in), a :: acc) 
        case Fail(in, _, _) => exhaust(p.parse(in.drop(1)), acc)
        case Partial(_)     => (r, acc)
      }
    def go(r: ParseResult[A]): Process1[String, A] =
      await1[String].flatMap { s =>
        val (r0, acc) = r match {
          case Done(in, a)    => (p.parse(in + s), List(a)) 
          case Fail(in, _, _) => (p.parse(in.drop(1) + s), Nil)
          case Partial(_)     => (r.feed(s), Nil)
        }
        val (r1, as) = exhaust(r0, acc)
        emitAll(as.reverse) ++ go(r1)
      } orElse emitAll(exhaust(r.done, Nil)._2)
    go(p.parse(""))  
  }

}
