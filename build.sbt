import UnidocKeys._
import ReleaseTransformations._

lazy val buildSettings = Seq(
	organization := "org.tpolecat",
	licenses ++= Seq(
		("MIT", url("http://opensource.org/licenses/MIT")),
		("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
	),
	scalaVersion := "2.12.2",
	crossScalaVersions := Seq("2.10.6", "2.11.11" scalaVersion.value),
  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.3" cross CrossVersion.binary)
)

lazy val commonSettings = Seq(
	scalacOptions ++= Seq(
		"-feature",
		"-deprecation",
		"-Yno-adapted-args",
		"-Ywarn-value-discard",
		"-Xfatal-warnings",
	  "-unchecked"
	),
	scalacOptions in compile ++= Seq(
		"-Yno-imports",
		"-Ywarn-numeric-widen"
	),
	parallelExecution in Test := false
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
      <url>git@github.com:tpolecat/atto.git</url>
      <connection>scm:git:git@github.com:tpolecat/atto.git</connection>
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
  .settings(unidocProjectFilter in (ScalaUnidoc, unidoc) := inAnyProject -- inProjects(testsJVM) -- inProjects(testsJS))
  .dependsOn(coreJVM, coreJS, testsJVM, testsJS, scalaz71, scalaz72JVM, scalaz72JS, catsJVM, catsJS)
  .aggregate(coreJVM, coreJS, testsJVM, testsJS, scalaz71, scalaz72JVM, scalaz72JS, catsJVM, catsJS)

lazy val core = crossProject.crossType(CrossType.Pure).in(file("modules/core"))
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-core")

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val tests = crossProject.crossType(CrossType.Pure).in(file("modules/tests")).dependsOn(core, scalaz72, cats)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
  .settings(name := "atto-tests")
  .settings(libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test")

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js

lazy val scalaz71 = project.in(file("modules/compat/scalaz71")).dependsOn(coreJVM)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-scalaz71")
  .settings(libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.1.11")

lazy val scalaz72 = crossProject.crossType(CrossType.Pure).in(file("modules/compat/scalaz72")).dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-scalaz72")
  .settings(libraryDependencies += "org.scalaz" %%% "scalaz-core" % "7.2.7")

lazy val scalaz72JVM = scalaz72.jvm
lazy val scalaz72JS = scalaz72.js

lazy val cats = crossProject.crossType(CrossType.Pure).in(file("modules/compat/cats")).dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-compat-cats")
  .settings(libraryDependencies += "org.typelevel" %%% "cats-core" % "0.9.0")

lazy val catsJVM = cats.jvm
lazy val catsJS = cats.js

lazy val docs = project.in(file("modules/docs")).dependsOn(coreJVM, scalaz71, catsJVM)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
  .settings(name := "atto-docs")
  .enablePlugins(MicrositesPlugin)
  .settings(
    micrositeName             := "atto",
    micrositeDescription      := "Everyday parsers.",
    micrositeAuthor           := "Rob Norris",
    micrositeGithubOwner      := "tpolecat",
    micrositeGithubRepo       := "atto",
    micrositeBaseUrl          := "/atto",
    micrositeDocumentationUrl := "/atto/docs/",
    micrositeHighlightTheme   := "color-brewer"
    // micrositePalette := Map(
    //   "brand-primary"     -> "#0B6E0B",
    //   "brand-secondary"   -> "#084D08",
    //   "brand-tertiary"    -> "#053605",
    //   "gray-dark"         -> "#453E46",
    //   "gray"              -> "#837F84",
    //   "gray-light"        -> "#E3E2E3",
    //   "gray-lighter"      -> "#F4F3F4",
    //   "white-color"       -> "#FFFFFF"
    // )
  )
