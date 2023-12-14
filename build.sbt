import Dependencies._

name := "play-test-ops-root"
scalaVersion := Scala_2_13

ThisBuild / organization := "com.rallyhealth"
ThisBuild / organizationName := "Rally Health"

ThisBuild / versionScheme := Some("early-semver")
ThisBuild / scalaVersion := Scala_2_13
ThisBuild / licenses += ("MIT", url("https://opensource.org/licenses/MIT"))

// reload sbt when the build files change
Global / onChangedBuildSource := ReloadOnSourceChanges

ThisBuild / homepage := Some(url("https://github.com/play-test-ops"))
// if you contribute to this library, please add yourself to this list!
ThisBuild / developers := List(
  Developer(id = "jeffmay", name = "Jeff May", email = "jeff.n.may@gmail.com", url = url("https://github.com/jeffmay")),
  Developer(id = "russellremple", name = "Russ Remple", email = "russell.remple@optum.com", url = url("https://github.com/russellremple")),
)

// scoverage and twirl depend different versions
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

// don't publish the jars for the root project (http://stackoverflow.com/a/8789341)
publish / skip := true

// don't search for previous artifact of the root project
mimaFailOnNoPrevious := false

def commonProject: Seq[Def.Setting[_]] =
  Seq(
    scalacOptions ++= Seq(
      "-encoding",
      "UTF-8",
      "-deprecation:false",
      "-feature",
      "-Xfatal-warnings",
    ) ++ (if (scalaBinaryVersion.value == "3") Seq() else Seq("-Ywarn-dead-code")),
    // Disable coverage for Scala 2.11 -- sbt-scoverage no longer supports it
    coverageEnabled := (if (scalaBinaryVersion.value == "2.11") false else coverageEnabled.value),
    // don't publish the test code
    Test / publishArtifact := false,
    // disable compilation of ScalaDocs, since this always breaks on links
    Compile / doc / sources := Seq.empty,
    // disable publishing empty ScalaDocs
    Compile / packageDoc / publishArtifact := false
  )

def coreProject(includePlayVersion: String): Seq[Def.Setting[_]] = {
  val playSuffix = includePlayVersion match {
    case Play_2_5 => "25"
    case Play_2_6 => "26"
    case Play_2_7 => "27"
    case Play_2_8 => "28"
    case Play_2_9 => "29"
    case Play_3_0 => "30"
  }
  val scalaVersions = includePlayVersion match {
    case Play_2_5 => Seq(Scala_2_11)
    case Play_2_6 => Seq(Scala_2_11, Scala_2_12)
    case Play_2_7 => Seq(Scala_2_11, Scala_2_12, Scala_2_13)
    case Play_2_8 => Seq(Scala_2_12, Scala_2_13)
    case Play_2_9 | Play_3_0 => Seq(Scala_2_13, Scala_3)
  }
  commonProject ++ Seq(
    name := s"play$playSuffix-test-ops-core",
    scalaVersion := scalaVersions.head,
    crossScalaVersions := scalaVersions,
    mimaPreviousArtifacts := Set(
      organization.value %% name.value % "1.5.0"
    ),
    // fail the build if the coverage drops below the minimum
    coverageMinimumStmtTotal := (
      // Right now Scala3 does not support a way to exclude packages or files from being instrumented.
      // https://github.com/scoverage/scalac-scoverage-plugin/blob/main/README.md
      if (scalaBinaryVersion.value == "3") 79
      else 90
      ),
    coverageFailOnMinimum := true,
    // add library dependencies
    libraryDependencies ++= Seq(
      playServer(includePlayVersion),
      playTest(includePlayVersion),
      scalaTest(includePlayVersion)
    )
  )
}

lazy val `play25-core` = project.settings(coreProject(Play_2_5))
lazy val `play26-core` = project.settings(coreProject(Play_2_6))
lazy val `play27-core` = project.settings(coreProject(Play_2_7))

lazy val `play28-core` = project.settings(coreProject(Play_2_8)).settings(
  // Akka references are in a separate source root to facilitate sharing other sources with the Play 3.0 project
  Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "scala-akka"
)

lazy val `play29-core` = project.settings(coreProject(Play_2_9)).settings(
  Compile / sourceDirectory := (`play28-core` / Compile / sourceDirectory).value,
  Test / sourceDirectory := (`play28-core` / Test / sourceDirectory).value,
  // Uses the same Akka type/object references as the Play 2.8 project
  Compile / unmanagedSourceDirectories += (Compile / sourceDirectory).value / "scala-akka"
)

lazy val `play30-core` = project.settings(coreProject(Play_3_0)).settings(
  // Uses locally-defined Pekko type/object references, so only reference non-Akka sources in the Play 2.8 project
  Compile / unmanagedSourceDirectories += (`play28-core` / Compile / sourceDirectory).value / "scala",
  Test / unmanagedSourceDirectories += (`play28-core` / Test / sourceDirectory).value / "scala"
)
