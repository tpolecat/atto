package atto.example

import atto._
import Atto._
import atto.syntax.stream.all._

import scala.App
import scalaz.stream._

object StreamExample extends App {

  // find and print the first 20 ints larger than 100 in /etc/services in constant space
  io.linesR("/etc/services")
    .parseLenient(int) // the new bit; the rest is vanilla streams
    .filter(_ > 100)
    .take(20)
    .map(_.toString + " ")
    .to(io.stdOut)
    .run
    .run

}