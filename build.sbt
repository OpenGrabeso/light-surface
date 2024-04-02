ThisBuild / githubOwner := "OpenGrabeso"

ThisBuild / githubRepository := "light-surface"

Global / excludeLintKeys += ThisBuild / githubTokenSource // prevent warning in SBT

def tokenSettings = Seq[Setting[_]](
  githubTokenSource := TokenSource.GitConfig("github.token") || TokenSource.Environment("BUILD_TOKEN") || TokenSource.Environment("GITHUB_TOKEN")
)

Global / excludeLintKeys += ThisBuild / githubTokenSource

publish / skip := true

publishLocal / skip := true

val VERSION = "0.5.3"
val SCALA_3 = "3.3.3"

// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// Disable the pipelining available since sbt-1.4.0. It caused compilation failure
ThisBuild / usePipelining := false

ThisBuild / scalaVersion := SCALA_3

ThisBuild / organization := "org.opengrabeso"

val buildSettings = tokenSettings ++ Seq[Setting[_]](
  version := VERSION,
  scmInfo := Some(ScmInfo(url("https://github.com/OpenGrabeso/light-surface"), "scm:git:github.com/OpenGrabeso/light-surface")),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  // Exclude compile-time only projects. This is a workaround for bloop,
  // which cannot resolve Optional dependencies nor compile-internal dependencies.
  crossPaths            := true,
  publishMavenStyle     := true,
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-release:8",
    // Use this flag for debugging Macros
    // "-Xcheck-macros",
    "-Wconf:msg=`_` is deprecated for wildcard arguments of types:s",
    "-Wconf:msg=with as a type operator has been deprecated:s",
    "-Wconf:msg=The syntax .* is no longer supported for vararg splices:s",
    "-Wconf:msg=Alphanumeric method .* is not declared infix:s",
  ),
)

// Do not run tests concurrently to avoid JMX registration failures
val runTestSequentially = Seq[Setting[_]](Test / parallelExecution := false)

val jsBuildSettings = Seq[Setting[_]](
  // #2117 For using java.util.UUID.randomUUID() in Scala.js
  libraryDependencies ++= Seq(
    ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0" % Test).cross(CrossVersion.for3Use2_13),
    // TODO It should be included in AirSpec
    "org.scala-js" %%% "scala-js-macrotask-executor" % "1.1.1" % Test
  ),
)

lazy val surface =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("airframe-surface"))
    .settings(buildSettings)
    .settings(
      name        := "light-surface",
      Compile / packageDoc / publishArtifact := false,
      description := "A library for extracting object structure surface",
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.18" % Test,
    )
    .jsSettings(jsBuildSettings)

lazy val root = project.in(file(".")).aggregate(surface.jvm, surface.js).settings(
  name := "light-surface",
  tokenSettings,
)

