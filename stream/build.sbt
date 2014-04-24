name := "atto-stream"

resolvers += "Scalaz Bintray Repo" at "http://dl.bintray.com/scalaz/releases"

libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.4.1"

initialCommands :=
  """import scalaz._
     import Scalaz._
     import scalaz.stream._
     import scalaz.stream.Process._
     import atto._
     import Atto._
     import atto.syntax.stream.all._
     import scalaz.concurrent.Task"""

bintrayPublishSettings
