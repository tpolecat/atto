name := "atto-stream"

libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.3"

initialCommands :=
  """import scalaz._
     import Scalaz._
     import scalaz.stream._
     import scalaz.stream.Process._
     import atto._
     import Atto._
     import atto.syntax.stream.all._
     import scalaz.concurrent.Task"""

