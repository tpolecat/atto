import sbtcrossproject.{ crossProject, CrossType }

lazy val catsVersion          = "2.2.0"
lazy val refinedVersion       = "0.9.17"
lazy val fs2CoreVersion       = "2.4.4"
lazy val scalacheckVersion    = "1.15.0"
lazy val kindProjectorVersion = "0.10.3"

inThisBuild(Seq(
  organization := "org.tpolecat",
  homepage     := Some(url("https://github.com/tpolecat/atto")),
  developers   := List(
    Developer("tpolecat", "Rob Norris", "rob_norris@mac.com", url("http://www.tpolecat.org"))
  ),
  licenses ++= Seq(
    ("MIT",     url("http://opensource.org/licenses/MIT")),
    ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
  ),
  scalaVersion        := "2.13.3",
  crossScalaVersions  := Seq("2.12.12", scalaVersion.value),
  libraryDependencies += compilerPlugin("org.typelevel" % "kind-projector" % kindProjectorVersion cross CrossVersion.binary),
  resolvers in Global += ("tpolecat" at "http://dl.bintray.com/tpolecat/maven").withAllowInsecureProtocol(true),
))

lazy val atto = // defined so we can exclude docs from aggregate
  project
    .in(file("."))
    .dependsOn(core.jvm, core.js, fs2.jvm, fs2.js, refined.jvm, refined.js, tests.jvm, tests.js)
    .aggregate(core.jvm, core.js, fs2.jvm, fs2.js, refined.jvm, refined.js, tests.jvm, tests.js)
    .settings(publish / skip := true)

lazy val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/core"))
    .settings(name := "atto-core")
    .settings(libraryDependencies += "org.typelevel" %%% "cats-core" % catsVersion)

lazy val fs2 =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/fs2"))
    .dependsOn(core)
    .settings(
      name := "atto-fs2",
      libraryDependencies ++= Seq(
        "co.fs2"         %%% "fs2-core" % fs2CoreVersion,
        "org.scalacheck" %%% "scalacheck" % scalacheckVersion % Test
      )
    )

lazy val refined =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/refined"))
    .dependsOn(core)
    .settings(
      name := "atto-refined",
      libraryDependencies += "eu.timepit" %%% "refined" % refinedVersion
    )

lazy val tests =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/tests"))
    .dependsOn(core, refined)
    .settings(
      name := "atto-tests",
      publish / skip := true,
      libraryDependencies += "org.scalacheck" %%% "scalacheck" % scalacheckVersion % Test
    )

lazy val docs = project
  .in(file("modules/docs"))
  .dependsOn(core.jvm, refined.jvm)
  .enablePlugins(ParadoxPlugin)
  .enablePlugins(ParadoxSitePlugin)
  .enablePlugins(GhpagesPlugin)
  .settings(
    scalacOptions      := Nil,
    git.remoteRepo     := "git@github.com:tpolecat/atto.git",
    ghpagesNoJekyll    := true,
    publish / skip     := true,
    paradoxTheme       := Some(builtinParadoxTheme("generic")),
    version            := version.value.takeWhile(_ != '+'), // strip off the +3-f22dca22+20191110-1520-SNAPSHOT business
    paradoxProperties ++= Map(
      "scala-versions"          -> (crossScalaVersions in core.jvm).value.map(CrossVersion.partialVersion).flatten.map(_._2).mkString("2.", "/", ""),
      "org"                     -> organization.value,
      "scala.binary.version"    -> s"2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "core-dep"                -> s"${(core.jvm / name).value}_2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "refined-dep"             -> s"${(refined.jvm / name).value}_2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "version"                 -> version.value,
      "scaladoc.atto.base_url" -> s"https://static.javadoc.io/org.tpolecat/atto-core_2.12/${version.value}",
    )
  )
