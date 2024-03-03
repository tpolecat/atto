import sbtcrossproject.{ crossProject, CrossType }

lazy val catsVersion       = "2.6.1"
lazy val fs2CoreVersion    = "2.5.6"
lazy val scalacheckVersion = "1.15.4"

lazy val scala212 = "2.12.16"
lazy val scala213 = "2.13.6"
lazy val scala30  = "3.3.3"

lazy val commonSettings = Seq(
  organization := "org.tpolecat",
  homepage     := Some(url("https://github.com/tpolecat/atto")),
  developers   := List(
    Developer("tpolecat", "Rob Norris", "rob_norris@mac.com", url("http://www.tpolecat.org"))
  ),
  licenses ++= Seq(
    ("MIT",     url("http://opensource.org/licenses/MIT")),
    ("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
  ),
  scalaVersion        := scala213,
  crossScalaVersions  := Seq(scala212, scala213, scala30),

  // dottydoc really doesn't work at all right now
  Compile / doc / sources := {
    val old = (Compile / doc / sources).value
    if (scalaVersion.value.startsWith("3."))
      Seq()
    else
      old
  },

)

lazy val atto = // defined so we can exclude docs from aggregate
  project
    .in(file("."))
    .dependsOn(core.jvm, core.js, fs2.jvm, fs2.js, refined.jvm, refined.js, tests.jvm, tests.js)
    .aggregate(core.jvm, core.js, fs2.jvm, fs2.js, refined.jvm, refined.js, tests.jvm, tests.js)
    .settings(commonSettings)
    .settings(publish / skip := true)

lazy val core =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/core"))
    .settings(commonSettings)
    .settings(name := "atto-core")
    .settings(libraryDependencies += "org.typelevel" %%% "cats-core" % catsVersion)

lazy val fs2 =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/fs2"))
    .dependsOn(core)
    .settings(commonSettings)
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
    .settings(commonSettings)
    .settings(
      name := "atto-refined",
      libraryDependencies += "eu.timepit" %%% "refined" % "0.9.26",
    )

lazy val tests =
  crossProject(JSPlatform, JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("modules/tests"))
    .dependsOn(core, refined)
    .settings(commonSettings)
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
  .settings(commonSettings)
  .settings(
    scalacOptions      := Nil,
    git.remoteRepo     := "git@github.com:tpolecat/atto.git",
    ghpagesNoJekyll    := true,
    publish / skip     := true,
    paradoxTheme       := Some(builtinParadoxTheme("generic")),
    version            := version.value.takeWhile(_ != '+'), // strip off the +3-f22dca22+20191110-1520-SNAPSHOT business
    paradoxProperties ++= Map(
      "scala-versions"          -> (core.jvm/crossScalaVersions).value.map(CrossVersion.partialVersion).flatten.map(_._2).mkString("2.", "/", ""),
      "org"                     -> organization.value,
      "scala.binary.version"    -> s"2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "core-dep"                -> s"${(core.jvm / name).value}_2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "refined-dep"             -> s"${(refined.jvm / name).value}_2.${CrossVersion.partialVersion(scalaVersion.value).get._2}",
      "version"                 -> version.value,
      "scaladoc.atto.base_url" -> s"https://static.javadoc.io/org.tpolecat/atto-core_2.12/${version.value}",
    )
  )
