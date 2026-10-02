name := "inoxcal"

version := "0.1"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.19" % Test,
  "com.lihaoyi" %% "mainargs" % "0.7.6",
  "dev.tamboui" % "tamboui-core" % "0.5.0"
)

// Main class for running the application
Compile / mainClass := Some("inoxcal.InoxcalApp")

// Keep the installed command and Universal distribution names stable for users.
executableScriptName := "inoxcal"
Universal / packageName := s"inoxcal-$version"
Universal / mappings ++= Seq(
  baseDirectory.value / "README.md" -> "README.md",
  baseDirectory.value / "LICENSE" -> "LICENSE"
)

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
