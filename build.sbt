import Dependencies._

name := "play-test-ops-root"

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

// set the scala version on the root project
ThisBuild / scalaVersion := Scala_2_11

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

// don't publish the jars for the root project (http://stackoverflow.com/a/8789341)
publish / skip := true
publishLocal / skip := true

/**
  * Semantic versioning attempts to validate that the version generated makes sense relative to previous
  * versions released. We are introducing support for new Scala versions in this release, so the semVerCheck
  * will fail. This setting will ensure that we don't forget to re-enable it after this release.
  */
val suppressSemVerCheckOfNewScalaVersionsUntilNextVersion = semVerCheck := {
  version.value match {
    case VersionNumber(Seq(1, 1, 3 | 4, _*), _, _) => Def.task {}
    case VersionNumber(Seq(1, 2, 0, _*), _, _) => Def.task {}
    case _ =>
      throw new RuntimeException(s"Version bump! Time to remove the suppression of semver checking.")
  }
  Def.taskDyn {
    scalaVersion.value match {
      case VersionNumber(Seq(2, 12, _*), _, _) => Def.task {}
      case _ => semVerCheck
    }
  }
}

def commonProject(id: String, path: String): Project = {
  Project(id, file(path)).settings(

    scalacOptions ++= Seq(
      "-encoding", "UTF-8",
      "-deprecation:false",
      "-feature",
      "-Xfatal-warnings",
      "-Ywarn-dead-code"
    ),

    // don't publish the test code
    Test / publishArtifact := false,

    // disable compilation of ScalaDocs, since this always breaks on links
    Compile / doc / sources := Seq.empty,

    // disable publishing empty ScalaDocs
    Compile / packageDoc / publishArtifact := false

  ).enablePlugins(SemVerPlugin)
}

def coreProject(includePlayVersion: String): Project = {
  val playSuffix = includePlayVersion match {
    case Play_2_5 => "25"
    case Play_2_6 => "26"
    case Play_2_7 => "27"
  }
  val scalaVersions = includePlayVersion match {
    case Play_2_5 => Seq(Scala_2_11)
    case Play_2_6 => Seq(Scala_2_11, Scala_2_12)
    case Play_2_7 => Seq(Scala_2_12, Scala_2_13)
  }
  val path = s"play$playSuffix-core"
  commonProject(path, path).settings(
    name := s"play$playSuffix-test-ops-core",
    crossScalaVersions := scalaVersions,
    // fail the build if the coverage drops below the minimum
    coverageMinimum := 80,
    coverageFailOnMinimum := true,
    // add library dependencies
    resolvers ++= Seq(
      "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
      Resolver.bintrayRepo("rallyhealth", "maven")
    ),
    libraryDependencies ++= Seq(
      playServer(includePlayVersion)
    ) ++ Seq(
      // Test-only dependencies
      playTest(includePlayVersion),
      scalaTest
    ).map(_ % Test)
  )
}

lazy val `play25-core` = coreProject(Play_2_5)
lazy val `play26-core` = coreProject(Play_2_6).settings(
  suppressSemVerCheckOfNewScalaVersionsUntilNextVersion
)
lazy val `play27-core` = coreProject(Play_2_7).settings(
  suppressSemVerCheckOfNewScalaVersionsUntilNextVersion
)
