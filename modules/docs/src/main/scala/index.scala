package docs

object index {

  //#sample
  import atto._, Atto._
  int.sepBy(spaceChar).parseOnly("1 20 300").option
  // res0: Option[List[Int]] = Some(List(1, 20, 300))
  //#sample

}