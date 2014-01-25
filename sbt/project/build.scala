import sbt._
import Keys._

object BuildSettings
{
  val buildOrganization = "edu.berkeley.cs"
  val buildVersion = "1.1"
  // val buildScalaVersion = "2.9.2"
  val buildScalaVersion = "2.10.1"
  def apply(srcpath: String) = {
    Defaults.defaultSettings ++ Seq (
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      // scalaSource in Compile := Path.absolute(file(projectdir + "/src/main/scala"))
      scalaSource in Compile := Path.absolute(file(srcpath))
      // libraryDependencies += "edu.berkeley.cs" %% "chisel" % "1.0"
    )
  }
}

object ChiselBuild extends Build
{
  import BuildSettings._

  lazy val music = Project("music", file("music"), settings = BuildSettings("../src")) dependsOn(chisel)
  lazy val chisel = Project("chisel", file("chisel"), settings = BuildSettings("../../chisel/src/main/scala"))
}
