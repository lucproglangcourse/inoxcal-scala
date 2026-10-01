name := "scalendar-scala"

version := "0.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.lihaoyi" %% "mainargs" % "0.7.6"
)

// Main class for running the application
Compile / mainClass := Some("scalendar.ScalendarApp")

// Run the tests in a forked JVM with a pinned locale, so that output assertions do
// not depend on the default locale of the machine or of CI (see
// doc/ai/20260930_RemediationPlan.md).
Test / fork := true
Test / javaOptions ++= Seq("-Duser.language=en", "-Duser.country=US")

// Fail `sbt coverage test coverageReport` (the command CI runs) when coverage drops
// below the current level: 96.4% statements / 96.2% branches at the time of writing.
coverageMinimumStmtTotal := 90
coverageMinimumBranchTotal := 85
coverageFailOnMinimum := true

enablePlugins(JavaAppPackaging)
