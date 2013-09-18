name := "scala-attoparsec"

version := "0.1"

scalaVersion := "2.10.2"

resolvers ++= Seq(
  "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
  "releases"  at "http://oss.sonatype.org/content/repositories/releases"
)

// Main
libraryDependencies ++= Seq(
  "org.scalaz"     %% "scalaz-core" % "7.0.2",
  "org.spire-math" %% "spire"       % "0.6.0"
)

// Test
libraryDependencies ++= Seq(     
  "org.scalacheck" %% "scalacheck" % "1.10.1" % "test",
  "org.specs2"     %% "specs2"     % "1.12.3" % "test"
)

// Let's add a linter
resolvers += "linter" at "http://hairyfotr.github.io/linteRepo/releases"

addCompilerPlugin("com.foursquare.lint" %% "linter" % "0.1-SNAPSHOT")

// And WartRemover (!)

resolvers += Resolver.sonatypeRepo("releases")

addCompilerPlugin("org.brianmckenna" % "wartremover" % "0.4" cross CrossVersion.full)

scalacOptions in (Compile, compile) += "-P:wartremover:traverser:org.brianmckenna.wartremover.warts.Unsafe"

// And turn warnings all the way up
scalacOptions ++= Seq(
	"-feature", 
	"-deprecation", 
	"-Ywarn-all", // doesn't actually turn them all on :-\
	"-Yno-adapted-args",
	"-Ywarn-value-discard", 
	"-Ywarn-numeric-widen",
	"-Ywarn-dead-code", // confused by ???, sadly
	"-Xlint",
	"-Xfatal-warnings"
)

initialCommands :=
  """import scalaz._
     import Scalaz._
     import atto._
     import Atto._"""


