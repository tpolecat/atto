name := "atto-example"

initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._
     import atto.parser.spire._
     import atto.syntax.stream._
     import example._"""

publishArtifact := false

tutSettings
