// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// Disable the pipelining available since sbt-1.4.0. It caused compilation failure
ThisBuild / usePipelining := false

ThisBuild / scalaVersion := "3.4.1"

val buildSettings = Seq[Setting[_]](
  version := "0.1.0-SNAPSHOT",
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-release:8",
    // Use this flag for debugging Macros
    // "-Xcheck-macros",
    "-Wconf:msg=`_` is deprecated for wildcard arguments of types:s",
    "-Wconf:msg=with as a type operator has been deprecated:s",
    "-Wconf:msg=The syntax .* is no longer supported for vararg splices:s",
    "-Wconf:msg=Alphanumeric method .* is not declared infix:s"
  )
)

lazy val root =
  project
    .in(file("airframe-surface"))
    .settings(buildSettings)
    .settings(
      name                                   := "light-surface",
      libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test
    )
