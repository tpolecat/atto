import sbt._

class attoparsec(info: ProjectInfo) extends DefaultProject(info) {
  val scalaz = "com.googlecode.scalaz" %% "scalaz-core" % "5.1-SNAPSHOT"

  override def managedStyle = ManagedStyle.Maven
  lazy val publishTo = Resolver.sftp("comonad.com Maven repository", "maven.comonad.com", "/home/ekmett/comonad.com/maven")
}
