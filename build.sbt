organization := "me.jeffmay"
organizationName := "Jeff May"

// set the scala version on the root project
scalaVersion := "2.11.7"

// don't publish the surrounding multi-project root
publish := {}

val commonSettings = Seq(

  scalaVersion := "2.11.7",

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

lazy val `play23-test-ops` = (project in file("play23"))
  .settings(commonSettings)
  .settings(
    name := "play-test-ops",
    version := "0.1.0",
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.3.10",
      "com.typesafe.play" %% "play-test" % "2.3.10" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0-RC4" % "test"
    )
  )
