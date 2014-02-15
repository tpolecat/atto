name := "atto-spire"

libraryDependencies ++= Seq(
  "org.spire-math" %% "spire" % "0.6.0"
)

initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._"""

