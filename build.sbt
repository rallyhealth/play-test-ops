
val commonRootSettings = Seq(
  organization := "me.jeffmay",
  organizationName := "Jeff May",
  
  // set the scala version on the root project
  scalaVersion := "2.11.8"
)

lazy val root = (project in file("."))
  .settings(commonRootSettings)
  .settings(
    name := "root",
    // don't publish the surrounding multi-project root
    publish := {}
  )
  .aggregate(
    `play23-core`,
    `play25-core`
  )

val commonSettings = commonRootSettings ++ Seq(

  version := "0.2.2",

  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-deprecation:false",
    "-feature",
    "-Xfatal-warnings",
    "-Ywarn-dead-code"
  ),

  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"
  ),

  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },

  // don't publish the test code
  publishArtifact in Test := false,

  // disable compilation of ScalaDocs, since this always breaks on links
  sources in(Compile, doc) := Seq.empty,

  // disable publishing empty ScalaDocs
  publishArtifact in (Compile, packageDoc) := false,

  licenses += ("Apache-2.0", url("http://opensource.org/licenses/apache-2.0"))
)

lazy val playVersion = settingKey[String]("The version of Play Framework")

val coreCommonSettings = commonSettings ++ Seq(
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % playVersion.value,
    "com.typesafe.play" %% "play-test" % playVersion.value % "test",
    "org.scalatest" %% "scalatest" % "3.0.0" % Test
  )
)

lazy val `play23-core` = (project in file("play23-core"))
  .settings(coreCommonSettings)
  .settings(
    name := "play23-test-ops-core",
    playVersion := "2.3.10"
  )

lazy val `play25-core` = (project in file("play25-core"))
  .settings(coreCommonSettings)
  .settings(
    name := "play25-test-ops-core",
    playVersion := "2.5.12"
  )
