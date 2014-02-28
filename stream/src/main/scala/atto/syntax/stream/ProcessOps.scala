package atto.syntax.stream

import scala.language.implicitConversions
import scala.language.higherKinds
import scalaz.syntax.Ops
import scalaz.stream.Process
import atto._
import atto.stream.Stream

trait ProcessOps[F[_]] extends Ops[Process[F, String]] {

  def parse1[A](p: Parser[A]): Process[F, ParseResult[A]] =
    self.pipe(Stream.parse1(p))

  def parseN[A](p: Parser[A]): Process[F, A] =
    self.pipe(Stream.parseN(p))

  def parseLenient[A](p: Parser[A]): Process[F, A] =
    self.pipe(Stream.parseLenient(p))

}

trait ToProcessOps {

  implicit def toProcessOps[F[_]](p: Process[F, String]): ProcessOps[F] =
    new ProcessOps[F] {
      val self = p
    }

}