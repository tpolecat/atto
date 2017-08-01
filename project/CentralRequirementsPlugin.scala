import sbt._, Keys._
import xerial.sbt.Sonatype.autoImport.sonatypeProfileName

object CentralRequirementsPlugin extends AutoPlugin {
  override def trigger = allRequirements
  override def requires = verizon.build.RigPlugin
  override lazy val projectSettings = Seq(
    sonatypeProfileName := "org.tpolecat",
    developers += Developer("tpolecat", "Rob Norris", "", url("http://github.com/tpolecat")),
    licenses ++= Seq(
  		("MIT", url("http://opensource.org/licenses/MIT")),
  		("BSD New", url("http://opensource.org/licenses/BSD-3-Clause"))
  	),
    homepage := Some(url("https://github.com/tpolecat/atto")),
    scmInfo := Some(ScmInfo(url("https://github.com/tpolecat/atto"), "git@github.com:tpolecat/atto.git"))
  )
}
