import UnidocKeys._
import ReleaseTransformations._

lazy val buildSettings = Seq(
	organization := "org.tpolecat",
	licenses ++= Seq(
		("MIT", url("http://opensource.org/licenses/MIT")),
		("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
	),
	scalaVersion := "2.11.7",
	crossScalaVersions := Seq("2.10.5", scalaVersion.value)
)

lazy val commonSettings = Seq(
	scalacOptions ++= Seq(
		"-feature", 
		"-deprecation", 
		"-Yno-adapted-args",
		"-Ywarn-value-discard", 
		"-Xlint",
		"-Xfatal-warnings",
	  "-unchecked"
	),
	scalacOptions in compile ++= Seq(
		"-Yno-imports",
		"-Ywarn-numeric-widen"
	)
)

lazy val tutSettings = buildSettings ++ commonSettings

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
  },
  publishArtifact in Test := false,
  homepage := Some(url("https://github.com/tpolecat/atto")),
  pomIncludeRepository := Function.const(false),
  pomExtra := (
    <scm>
      <url>git@github.com:tpolecat/tut.git</url>
      <connection>scm:git:git@github.com:tpolecat/tut.git</connection>
    </scm>
    <developers>
      <developer>
        <id>tpolecat</id>
        <name>Rob Norris</name>
        <url>http://tpolecat.org</url>
      </developer>
    </developers>
  ),
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    ReleaseStep(action = Command.process("package", _)),
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    ReleaseStep(action = Command.process("publishSigned", _)),
    setNextVersion,
    commitNextVersion,
    ReleaseStep(action = Command.process("sonatypeReleaseAll", _)),
    pushChanges)
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val atto = project.in(file("."))
  .settings(tutSettings)
  .settings(noPublishSettings)
  .settings(unidocSettings)
  .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(example))
  .dependsOn(core, spire, stream, example)
  .aggregate(core, spire, stream, example)

lazy val core = project.in(file("core"))
  .settings(tutSettings ++ publishSettings)
  .settings(name := "atto-core")
  .settings(
  	libraryDependencies ++= Seq(
      "org.scalaz"     %% "scalaz-core" % "7.2.1",
      "org.scalacheck" %% "scalacheck"  % "1.13.1" % "test"
		)
	)
	.settings(initialCommands := "import scalaz._, Scalaz._, atto._, Atto._")

lazy val spire = project.in(file("spire")).dependsOn(core)
  .settings(tutSettings ++ publishSettings)
	.settings(name := "atto-spire")
	.settings(libraryDependencies +=  "org.spire-math" %% "spire" % "0.11.0")
	.settings(initialCommands := "import scalaz._, Scalaz._, atto._, Atto._")

lazy val stream = project.in(file("stream")).dependsOn(core)
  .settings(tutSettings ++ publishSettings)
	.settings(name := "atto-stream")
	.settings(libraryDependencies += "org.scalaz.stream" %% "scalaz-stream" % "0.7.2a")
	.settings(initialCommands :=
	  """import scalaz._
	     import Scalaz._
	     import scalaz.stream._
	     import scalaz.stream.Process._
	     import atto._
	     import Atto._
	     import atto.syntax.stream.all._
	     import scalaz.concurrent.Task"""	  
   )

lazy val example = project.in(file("example")).dependsOn(core, spire, stream)
  .settings(tutSettings ++ noPublishSettings)
  .settings(name := "atto-example")

