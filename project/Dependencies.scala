import sbt._

object Dependencies {

  final val Scala_2_11 = "2.11.12"
  final val Scala_2_12 = "2.12.6"

  final val Play_2_3 = "2.3.10"
  final val Play_2_5 = "2.5.19"
  final val Play_2_6 = "2.6.19"

  def playServer(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play" % includePlayVersion
  }

  def playTest(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play-test" % includePlayVersion
  }

  val scalaTest: ModuleID = {
    "org.scalatest" %% "scalatest" % "3.0.5"
  }
}
