import atto._, Atto._
import cats._, cats.implicits._
import scala.language.higherKinds

sealed trait Expr[A] {
  def map[B](f: A => B): Expr[B] =
    this match {
      case Lit(a) => Lit(a)
      case Mul(a, b) => Mul(f(a), f(b))
      case Div(a, b) => Div(f(a), f(b))
      case Add(a, b) => Add(f(a), f(b))
      case Sub(a, b) => Sub(f(a), f(b))
    }
}

case class Lit[A](i: Int)     extends Expr[A]
case class Mul[A](a: A, b: A) extends Expr[A]
case class Div[A](a: A, b: A) extends Expr[A]
case class Add[A](a: A, b: A) extends Expr[A]
case class Sub[A](a: A, b: A) extends Expr[A]

object Expr {
  implicit val ExprFunctor: Functor[Expr] =
    new Functor[Expr] {
      def map[A, B](fa: Expr[A])(f: A => B) = fa.map(f)
    }
}

object Example extends App {

  case class Fix[F[_]](unfix: F[Fix[F]]) {
    def cata[A](f: F[A] => A)(implicit ev: Functor[F]): A =
      f(unfix.map(_.cata(f)))
  }

  type P = Parser[Fix[Expr]]

  val num: P = int.map(n => Fix(Lit(n)))
  val mul: P = num.sepBy1(char('*')).map(nel => nel.reduceLeft((a, b) => Fix(Mul(a, b))))
  val add: P = mul.sepBy1(char('+')).map(nel => nel.reduceLeft((a, b) => Fix(Add(a, b))))

  val e: Fix[Expr] = add.parseOnly("1+2*3+10").option.get

  val n = e.cata[Int] {
    case Lit(a)    => a
    case Mul(a, b) => a * b
    case Div(a, b) => a / b
    case Add(a, b) => a + b
    case Sub(a, b) => a - b
  }

  val m = e.cata[Either[String, Int]] {
    case Lit(a)    => Right(a)
    case Mul(a, b) => (a |@| b).map(_ * _)
    case Div(a, Right(0)) => Left("Division by Zero")
    case Div(a, b) => (a |@| b).map(_ / _)
    case Add(a, b) => (a |@| b).map(_ + _)
    case Sub(a, b) => (a |@| b).map(_ - _)
  }

  println(n)
  println(m)

}
