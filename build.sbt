lazy val releaseVersionCheck = taskKey[Unit]("Validate the release tag against the effective sbt version")
lazy val releaseTag = settingKey[String]("Release tag supplied by the release workflow")

import sbtrelease.ReleaseStateTransformations.*

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging)
  .settings(
    // Project metadata and Scala compiler policy.
    name := "inoxcal",
    scalaVersion := "3.8.4",
    scalacOptions ++= Seq(
      "-deprecation",
      "-feature",
      "-unchecked",
      "-Wvalue-discard",
      "-language:strictEquality",
      "-Yexplicit-nulls",
      "-Wsafe-init"
    ),

    // Application dependencies.
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.2.20" % Test,
      "com.lihaoyi" %% "mainargs" % "0.7.6",
      "dev.tamboui" % "tamboui-core" % "0.5.0"
    ),

    // Main class and distribution packaging.
    Compile / mainClass := Some("inoxcal.InoxcalApp"),
    executableScriptName := "inoxcal",
    Universal / packageName := s"inoxcal-${version.value}",
    Universal / mappings ++= Seq(
      (baseDirectory.value / "README.md") -> "README.md",
      (baseDirectory.value / "LICENSE") -> "LICENSE"
    ),

    // Run tests with a pinned locale so output assertions do not depend on the
    // machine running them (see doc/ai/20260930_RemediationPlan.md).
    Test / fork := true,
    Test / javaOptions ++= Seq("-Duser.language=en", "-Duser.country=US"),

    // Fail coverage commands when coverage drops below the configured gates.
    coverageMinimumStmtTotal := 90,
    coverageMinimumBranchTotal := 85,
    coverageFailOnMinimum := true,

    // Let sbt-release manage version.sbt, commits, and tags. Archive publication
    // remains the responsibility of the tag-triggered GitHub Actions workflow.
    releaseProcess := Seq(
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion
    ),

    releaseTag := sys.props.getOrElse("releaseTag", ""),
    releaseVersionCheck := {
      val suppliedTag = releaseTag.value
      val expectedVersion = suppliedTag.stripPrefix("v")
      require(
        suppliedTag.nonEmpty && expectedVersion == version.value,
        s"release tag '$suppliedTag' does not match sbt version '${version.value}'"
      )
    }
  )
