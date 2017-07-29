import cats.Eq, cats.implicits._

package object atto {

  private[atto] implicit class ParseResultOps[A: Eq](a: ParseResult[A]) {
    import ParseResult._
    def sameAs(b: ParseResult[A]) =
      (a, b) match {
        case (Fail(a, b, c), Fail(d, e, f)) => (a, b, c).===((d, e, f))
        case (Done(a, b), Done(d, e)) => (a, b).===((d, e)   )
        case (Partial(_), Partial(_)) => sys.error("undefined")
        case _ => false
      }
  }

}
