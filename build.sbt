ThisBuild / scalaVersion := "2.13.2"

homepage in ThisBuild := Some(url("https://github.com/synesso/scala-stellar-sdk"))

lazy val root = project
  .in(file("."))
  .aggregate(protocol)

lazy val protocol = project
  .in(file("protocol"))
  .settings(
    libraryDependencies ++= List(
      "commons-codec" % "commons-codec" % "1.14",
      "org.typelevel" %% "cats-core" % "2.1.1",
      "net.i2p.crypto" % "eddsa" % "0.3.0",
      "com.squareup.okio" % "okio" % "2.6.0"
    ) ::: logging ::: specs2,
    coverage(99)
  )

/*
lazy val core = project
  .in(file("core"))
  .dependsOn(xdr)

lazy val testing = project
  .in(file("testing"))

lazy val integration = project
  .in(file("integration"))
  .dependsOn(core)
*/

val logging = List(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
)
val specs2 = List(
  "org.specs2" %% "specs2-core" % "4.9.3" % "test",
  "org.specs2" %% "specs2-scalacheck" % "4.9.3" % "test",
)

def coverage(min: Int) = List(
  coverageMinimum := min,
  coverageFailOnMinimum := true,
)
