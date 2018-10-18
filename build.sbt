import Dependencies._

name := "play-test-ops-root"

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

// set the scala version on the root project
ThisBuild / scalaVersion := Scala_2_11

ThisBuild / bintrayOrganization := Some("rallyhealth")
ThisBuild / bintrayRepository := "maven"

ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

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
    case Play_2_3 => "23"
    case Play_2_5 => "25"
    case Play_2_6 => "26"
  }
  val scalaVersions = includePlayVersion match {
    case Play_2_3 | Play_2_5 => Seq(Scala_2_11)
    case Play_2_6 => Seq(Scala_2_11, Scala_2_12)
  }
  val path = s"play$playSuffix-core"
  commonProject(path, path).settings(
    name := s"play$playSuffix-test-ops-core",
    scalaVersion := scalaVersions.head,
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

lazy val `play23-core` = coreProject(Play_2_3)
lazy val `play25-core` = coreProject(Play_2_5)
lazy val `play26-core` = coreProject(Play_2_6)

// don't publish the root project
publish := {}
publishLocal := {}
