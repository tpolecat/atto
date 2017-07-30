import ReleaseTransformations._

// Only run WartRemover on 2.12
def attoWarts(sv: String) =
  sv match {
    case "2.10.6"  |
         "2.11.11" => Nil
    case "2.12.3" | "2.13.0-M1"  =>
      Warts.allBut(
        Wart.Nothing,            // false positives
        Wart.DefaultArguments,   // used for labels in a bunch of places
        Wart.ImplicitConversion  // we know what we're doing
      )
  }

lazy val compilerFlags = Seq(
  scalacOptions ++= (
    scalaVersion.value match {
      case "2.10.6"  |
           "2.11.11" =>
        Seq(
          "-feature",
          "-deprecation",
          "-Yno-adapted-args",
          "-Ywarn-value-discard",
          "-Xlint",
          "-Xfatal-warnings",
          "-unchecked",
          "-Yno-imports",
          "-Ywarn-numeric-widen"
        )
      case "2.12.3" | "2.13.0-M1" =>
        Seq(
          "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
          "-encoding", "utf-8",                // Specify character encoding used by source files.
          "-explaintypes",                     // Explain type errors in more detail.
          "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
          "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
          "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
          "-language:higherKinds",             // Allow higher-kinded types
          "-language:implicitConversions",     // Allow definition of implicit functions called views
          "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
          "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
          "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
          "-Xfuture",                          // Turn on future language features.
          "-Xlint:adapted-args",               // Warn if an argument list is modified to match the receiver.
          "-Xlint:by-name-right-associative",  // By-name parameter of right associative operator.
          "-Xlint:constant",                   // Evaluation of a constant arithmetic expression results in an error.
          "-Xlint:delayedinit-select",         // Selecting member of DelayedInit.
          "-Xlint:doc-detached",               // A Scaladoc comment appears to be detached from its element.
          "-Xlint:inaccessible",               // Warn about inaccessible types in method signatures.
          "-Xlint:infer-any",                  // Warn when a type argument is inferred to be `Any`.
          "-Xlint:missing-interpolator",       // A string literal appears to be missing an interpolator id.
          "-Xlint:nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Xlint:nullary-unit",               // Warn when nullary methods return Unit.
          "-Xlint:option-implicit",            // Option.apply used implicit view.
          "-Xlint:package-object-classes",     // Class or object defined in package object.
          "-Xlint:poly-implicit-overload",     // Parameterized overloaded implicit methods are not visible as view bounds.
          "-Xlint:private-shadow",             // A private field (or class parameter) shadows a superclass field.
          "-Xlint:stars-align",                // Pattern sequence wildcard must align with sequence component.
          "-Xlint:type-parameter-shadow",      // A local type parameter shadows a type already in scope.
          "-Xlint:unsound-match",              // Pattern match may not be typesafe.
          "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
          "-Yno-imports",                      // No predef or default imports
          "-Ypartial-unification",             // Enable partial unification in type constructor inference
          "-Ywarn-dead-code",                  // Warn when dead code is identified.
          "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
          "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
          "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
          "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
          "-Ywarn-numeric-widen",              // Warn when numerics are widened.
          "-Ywarn-unused:implicits",           // Warn if an implicit parameter is unused.
          "-Ywarn-unused:imports",             // Warn if an import selector is not referenced.
          "-Ywarn-unused:locals",              // Warn if a local definition is unused.
          "-Ywarn-unused:params",              // Warn if a value parameter is unused.
          "-Ywarn-unused:patvars",             // Warn if a variable bound in a pattern is unused.
          "-Ywarn-unused:privates",            // Warn if a private member is unused.
          "-Ywarn-value-discard"               // Warn when non-Unit expression results are unused.
        )
    }
  ),
  scalacOptions in (Test, compile) --= (
    scalaVersion.value match {
      case "2.10.6"  |
           "2.11.11" => Seq("-Yno-imports")
      case "2.12.3"  =>
        Seq(
          "-Ywarn-unused:privates",
          "-Ywarn-unused:locals",
          "-Ywarn-unused:imports",
          "-Yno-imports"
        )
    }
  ),
  scalacOptions in (Compile, console) --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports"),
  scalacOptions in (Tut, tut)         --= Seq("-Xfatal-warnings", "-Ywarn-unused:imports", "-Yno-imports")
)

lazy val buildSettings = Seq(
	organization := "org.tpolecat",
	licenses ++= Seq(
		("MIT", url("http://opensource.org/licenses/MIT")),
		("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
	),
	scalaVersion := "2.12.3",
	crossScalaVersions := Seq("2.10.6", "2.11.11", scalaVersion.value),
  addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.4" cross CrossVersion.binary)
)

lazy val commonSettings =
	compilerFlags ++ Seq(
    wartremoverErrors in (Compile, compile) := attoWarts(scalaVersion.value),
    wartremoverErrors in (Test,    compile) := attoWarts(scalaVersion.value),
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
  .dependsOn(coreJVM, coreJS, testsJVM, testsJS)
  .aggregate(coreJVM, coreJS, testsJVM, testsJS)

lazy val core = crossProject.crossType(CrossType.Pure).in(file("modules/core"))
  .settings(buildSettings ++ commonSettings ++ publishSettings)
  .settings(name := "atto-core")
	.settings(libraryDependencies += "org.typelevel" %%% "cats-core" % "0.9.0")

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val tests = crossProject.crossType(CrossType.Pure).in(file("modules/tests"))
	.dependsOn(core)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
	.settings(libraryDependencies += "org.scalacheck" %%% "scalacheck" % "1.13.4" % "test")
  .settings(name := "atto-tests")

lazy val testsJVM = tests.jvm
lazy val testsJS = tests.js

lazy val docs = project.in(file("modules/docs")).dependsOn(coreJVM)
  .settings(buildSettings ++ commonSettings ++ noPublishSettings)
  .settings(
		name := "atto-docs",
		scalacOptions -= "-Xlint"
	)
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
