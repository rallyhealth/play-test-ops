import sbt._

object Dependencies {

  final val Scala_2_11 = "2.11.12"
  // Can't upgrade to Scala 2.12.13 because of https://github.com/scoverage/sbt-scoverage/issues/321
  final val Scala_2_12 = "2.12.12"
  final val Scala_2_13 = "2.13.5"

  final val Play_2_5 = "2.5.19"
  final val Play_2_6 = "2.6.25"
  final val Play_2_7 = "2.7.9"
  final val Play_2_8 = "2.8.7"

  def playServer(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play" % includePlayVersion
  }

  def playTest(includePlayVersion: String): ModuleID = {
    "com.typesafe.play" %% "play-test" % includePlayVersion
  }

  val scalaTest: ModuleID = {
    "org.scalatest" %% "scalatest" % "3.2.7"
  }
}
