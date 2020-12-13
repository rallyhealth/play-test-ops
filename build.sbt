import Dependencies._

name := "play-test-ops-root"

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / scalaVersion := "2.13.5"

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

// reload sbt when the build files change
Global / onChangedBuildSource := ReloadOnSourceChanges

// don't publish the jars for the root project (http://stackoverflow.com/a/8789341)
publish / skip := true
publishLocal / skip := true

// don't search for previous artifact of the root project
mimaFailOnNoPrevious := false

def commonProject(id: String, path: String): Project = {
  Project(id, file(path)).settings(
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
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
  )
}

def coreProject(
  includePlayVersion: String,
  previousVersions: Set[String] = Set("1.4.0"),
): Project = {
  val playSuffix = includePlayVersion match {
    case Play_2_5 => "25"
    case Play_2_6 => "26"
    case Play_2_7 => "27"
    case Play_2_8 => "28"
  }
  val scalaVersions = includePlayVersion match {
    case Play_2_5 => Seq(Scala_2_11)
    case Play_2_6 => Seq(Scala_2_11, Scala_2_12)
    case Play_2_7 => Seq(Scala_2_11, Scala_2_12, Scala_2_13)
    case Play_2_8 => Seq(Scala_2_12, Scala_2_13)
  }
  val path = s"play$playSuffix-core"
  commonProject(path, path).settings(
    name := s"play$playSuffix-test-ops-core",
    scalaVersion := scalaVersions.head,
    crossScalaVersions := scalaVersions,
    mimaPreviousArtifacts := previousVersions.map(
      "com.rallyhealth" %% name.value % _
    ),
    // fail the build if the coverage drops below the minimum
    coverageMinimum := 80,
    coverageFailOnMinimum := true,
    // add library dependencies
    resolvers ++= Seq(
      // TODO: Remove this after next release. This is only needed to find the previous version for mima.
      Resolver.bintrayRepo("rallyhealth", "maven"),
      "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
    ),
    libraryDependencies ++= Seq(playServer(includePlayVersion)) ++ Seq(
      // Test-only dependencies
      playTest(includePlayVersion),
      scalaTest
    ).map(_ % Test)
  )
}

lazy val `play25-core` = coreProject(Play_2_5)
lazy val `play26-core` = coreProject(Play_2_6)
lazy val `play27-core` = coreProject(Play_2_7)
lazy val `play28-core` = coreProject(Play_2_8).settings(
  libraryDependencies ++= Seq(playTest(Play_2_8))
)
