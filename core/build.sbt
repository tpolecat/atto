name := "atto-core"

libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core" % "7.1.0",
  "org.scalacheck" %% "scalacheck"  % "1.11.3" % "test"
)

initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._"""

bintrayPublishSettings

