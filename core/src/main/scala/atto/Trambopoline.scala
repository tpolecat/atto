package atto

import scala.annotation.tailrec

// trampoline derived without shame or permission from cats.Eval
sealed abstract class Trambopoline[A] { self =>

  def run: A

  def map[B](f: A => B): Trambopoline[B] =
    flatMap(a => Trambopoline.Done(f(a)))

  def flatMap[B](f: A => Trambopoline[B]): Trambopoline[B] =
    this match {
      case c: Trambopoline.Moar[A] =>
        new Trambopoline.Moar[B] {
          type Start = c.Start
          // See https://issues.scala-lang.org/browse/SI-9931 for an explanation
          // of why the type annotations are necessary in these two lines on
          // Scala 2.12.0.
          val start: () => Trambopoline[Start] = c.start
          val go: Start => Trambopoline[B] = (s: c.Start) =>
            new Trambopoline.Moar[B] {
              type Start = A
              val start = () => c.go(s)
              val go = f
            }
        }
      case _ =>
        new Trambopoline.Moar[B] {
          type Start = A
          val start = () => self
          val go = f
        }
    }

}

object Trambopoline {

  val unit = done(())

  def done[A](a: A): Trambopoline[A] = Done(a)

  def suspend[A](a: => Trambopoline[A]): Trambopoline[A] =
    unit.flatMap(_ => a)

  private final case class Done[A](run: A) extends Trambopoline[A]

  private sealed abstract class Moar[A] extends Trambopoline[A] {
    type Start
    val start: () => Trambopoline[Start]
    val go: Start => Trambopoline[A]
    def run: A = {
      type L = Trambopoline[Any]
      type C = Any => Trambopoline[Any]
      @tailrec def loop(curr: L, fs: List[C]): Any =
        curr match {
          case c: Moar[_] =>
            c.start() match {
              case cc: Moar[_] =>
                loop(
                  cc.start().asInstanceOf[L],
                  cc.go.asInstanceOf[C] :: c.go.asInstanceOf[C] :: fs)
              case xx =>
                loop(c.go(xx.run).asInstanceOf[L], fs)
            }
          case x =>
            fs match {
              case f :: fs => loop(f(x.run), fs)
              case Nil => x.run
            }
        }
      loop(this.asInstanceOf[L], Nil).asInstanceOf[A]
    }
  }

}
