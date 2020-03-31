import sbt._

object Dependencies {

  final val Scala_2_11 = "2.11.12"
  final val Scala_2_12 = "2.12.6"
  final val Scala_2_13 = "2.13.1"

  final val Play_2_5 = "2.5.19"
  final val Play_2_6 = "2.6.19"
  final val Play_2_7 = "2.7.4"

  def playServer(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play" % includePlayVersion
  }

  def playTest(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play-test" % includePlayVersion
  }

  val scalaTest: ModuleID = {
    "org.scalatest" %% "scalatest" % "3.1.1"
  }
}
