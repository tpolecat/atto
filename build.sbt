name := "atto"

description := "functional parser combinators for scala"

organization in ThisBuild := "org.tpolecat"

version in ThisBuild := "0.2-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.3"

licenses in ThisBuild += ("MIT", url("http://opensource.org/licenses/MIT"))

scalacOptions in ThisBuild ++= Seq(
	"-feature", 
	"-deprecation", 
	"-Ywarn-all", 
	"-Yno-adapted-args",
	"-Ywarn-value-discard", 
	"-Ywarn-numeric-widen",
	"-Ywarn-dead-code", 
	"-Xlint",
	"-Xfatal-warnings",
  "-unchecked"
)

lazy val core = project.in(file("core"))

lazy val spire = project.in(file("spire")).dependsOn(core)

lazy val stream = project.in(file("stream")).dependsOn(core)

lazy val example = project.in(file("example")).dependsOn(core, spire, stream)

publishArtifact := false

// Bintray
seq(bintrayPublishSettings:_*)
