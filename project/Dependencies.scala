import sbt._

object Dependencies {

  final val Scala_2_11 = "2.11.12"
  // Can't upgrade to Scala 2.12.13 because of https://github.com/scoverage/sbt-scoverage/issues/321
  final val Scala_2_12 = "2.12.12"
  final val Scala_2_13 = "2.13.12"
  final val Scala_3 = "3.3.1"

  final val Play_2_5 = "2.5.19"
  final val Play_2_6 = "2.6.25"
  final val Play_2_7 = "2.7.9"
  final val Play_2_8 = "2.8.21"
  final val Play_2_9 = "2.9.0"
  final val Play_3_0 = "3.0.0"

  def playOrg(includePlayVersion: String): String = includePlayVersion match {
    case Play_3_0 => "org.playframework"
    case _ => "com.typesafe.play"
  }

  def playServer(includePlayVersion: String): ModuleID = {
    playOrg(includePlayVersion) %% "play" % includePlayVersion
  }

  // Test config only through Play 2.7
  def playTest(includePlayVersion: String): ModuleID = {
    val module = playOrg(includePlayVersion) %% "play-test" % includePlayVersion
    includePlayVersion match {
      case Play_2_8 | Play_2_9 | Play_3_0 => module
      case _ => module % Test
    }
  }

  // Test-only dependency
  def scalaTest(includePlayVersion: String): ModuleID = {
    val scalatestVersion = includePlayVersion match {
      case Play_2_9 | Play_3_0 => "3.2.15"
      case _ => "3.2.7"
    }
    "org.scalatest" %% "scalatest" % scalatestVersion % Test
  }
}
