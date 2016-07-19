name := "play-test-ops"
organization := "me.jeffmay"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.7",
  scalacOptions ++= Seq(
    "-encoding", "UTF-8",
    "-deprecation:false",
    "-feature",
    "-Xfatal-warnings",
    "-Ywarn-dead-code"
  )
)

lazy val `play23-test-ops` = (project in file("play23"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play" % "2.3.10",
      "com.typesafe.play" %% "play-test" % "2.3.10" % "test",
      "org.scalatest" %% "scalatest" % "3.0.0-RC4" % "test"
    )
  )

