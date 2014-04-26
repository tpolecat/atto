name := "atto-example"

initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._
     import atto.parser.spire._
     import atto.syntax.stream._
     import example._
     import JsonExample._
     import JsonTest._
     def time[A](a: => A) = { def now = System.currentTimeMillis; val t = now; a; now - t }"""

publishArtifact := false

tutSettings

