package docs

object refined_integration {

  //#imports
  import atto._, Atto._
  import atto.syntax.refined._
  import eu.timepit.refined.numeric._
  //#imports

  //#positiveInt
  val positiveInt = int.refined[Positive]
  //#positiveInt

  //#positiveInt-parse-1
  positiveInt parseOnly "123"
  // res0: ParseResult[Refined[Int, Positive]] = Done(,123)
  //#positiveInt-parse-1

  //#positiveInt-parse-2
  positiveInt parseOnly "-123"
  // res1: ParseResult[Refined[Int, Positive]] = Fail(,List(),Predicate failed: (-123 > 0).)
  //#positiveInt-parse-2

}