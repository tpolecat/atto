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
  .settings(buildSettings ++ commonSettings)
  .settings(noPublishSettings)
  .settings(unidocSettings)
  .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(tests))
  .dependsOn(core, tests, scalaz71, scalaz72, cats04)
  .aggregate(core, tests, scalaz71, scalaz72, cats04)

lazy val core = project.in(file("core"))
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-core")

lazy val tests = project.in(file("tests")).dependsOn(core, scalaz71)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
  .settings(name := "atto-tests")
  .settings(libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.11.3" % "test")

lazy val scalaz71 = project.in(file("compat/scalaz71")).dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-scalaz71")
  .settings(libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.6")

lazy val scalaz72 = project.in(file("compat/scalaz72")).dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-scalaz72")
  .settings(libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.1")

lazy val cats04 = project.in(file("compat/cats04")).dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-cats04")
  .settings(libraryDependencies += "org.typelevel" %% "cats" % "0.4.1")


