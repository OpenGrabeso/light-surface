ThisBuild / githubOwner := "OpenGrabeso"

ThisBuild / githubRepository := "light-surface"

Global / excludeLintKeys += ThisBuild / githubTokenSource // prevent warning in SBT

def tokenSettings = Seq[Setting[_]](
  githubTokenSource := TokenSource.GitConfig("github.token") || TokenSource.Environment("BUILD_TOKEN") || TokenSource.Environment("GITHUB_TOKEN")
)

Global / excludeLintKeys += ThisBuild / githubTokenSource

publish / skip := true

publishLocal / skip := true

val VERSION = "0.0.2"
val SCALA_2_13          = "2.13.13"
val SCALA_3_4             = "3.4.1-RC1"
val targetScalaVersions = SCALA_3_4 :: SCALA_2_13 :: Nil

// Add this for using snapshot versions
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

val AIRFRAME_VERSION                 = sys.env.getOrElse("AIRSPEC_VERSION", "24.2.3")
val SCALACHECK_VERSION              = "1.17.0"
val JS_JAVA_LOGGING_VERSION         = "1.0.0"
val JAVAX_ANNOTATION_API_VERSION    = "1.3.2"

// Allow using Ctrl+C in sbt without exiting the prompt
// Global / cancelable := true

//ThisBuild / turbo := true

// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// Disable the pipelining available since sbt-1.4.0. It caused compilation failure
ThisBuild / usePipelining := false

// Use Scala 3 by default as scala-2 specific source code is relatively small now
ThisBuild / scalaVersion := SCALA_3_4

ThisBuild / organization := "org.opengrabeso"

val buildSettings = tokenSettings ++ Seq[Setting[_]](
  version := VERSION,
  scmInfo := Some(ScmInfo(url("https://github.com/OpenGrabeso/light-surface"), "scm:git:github.com/OpenGrabeso/light-surface")),
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  // Exclude compile-time only projects. This is a workaround for bloop,
  // which cannot resolve Optional dependencies nor compile-internal dependencies.
  crossScalaVersions    := targetScalaVersions,
  crossPaths            := true,
  publishMavenStyle     := true,
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation",
    "-release:8",
    // Use this flag for debugging Macros
    // "-Xcheck-macros",
  ) ++ {
    if (scalaVersion.value.startsWith("3.")) {
      Seq(
        "-Wconf:msg=`_` is deprecated for wildcard arguments of types:s",
        "-Wconf:msg=with as a type operator has been deprecated:s",
        "-Wconf:msg=The syntax .* is no longer supported for vararg splices:s",
        "-Wconf:msg=Alphanumeric method .* is not declared infix:s",
      )
    } else {
      Seq(
        // Necessary for tracking source code range in airframe-rx demo
        "-Yrangepos",
        // For using the new import * syntax even in Scala 2.x
        "-Xsource:3"
      )
    }
  },
  testFrameworks += new TestFramework("wvlet.airspec.Framework"),
  libraryDependencies ++= Seq(
    "org.wvlet.airframe" %%% "airspec"    % AIRFRAME_VERSION    % Test,
    "org.scalacheck"     %%% "scalacheck" % SCALACHECK_VERSION % Test
  ) ++ {
    if (scalaVersion.value.startsWith("3."))
      Seq.empty
    else
      Seq("org.scala-lang.modules" %%% "scala-collection-compat" % "2.11.0")
  }
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

val surfaceDependencies = { scalaVersion: String =>
  scalaVersion match {
    case s if s.startsWith("3.") =>
      Seq.empty
    case _ =>
      Seq(
        ("org.scala-lang" % "scala-reflect"  % scalaVersion),
        ("org.scala-lang" % "scala-compiler" % scalaVersion % Provided)
      )
  }
}

val surfaceJVMDependencies = { scalaVersion: String =>
  scalaVersion match {
    case s if s.startsWith("3.") =>
      Seq(
        "org.scala-lang" %% "scala3-tasty-inspector" % s,
        "org.scala-lang" %% "scala3-staging"         % s
      )
    case _ => Seq.empty
  }
}

lazy val surface =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("airframe-surface"))
    .settings(buildSettings)
    .settings(
      name        := "light-surface",
      Compile / packageDoc / publishArtifact := false,
      description := "A library for extracting object structure surface",
      // TODO: This is a temporary solution. Use AirSpec after Scala 3 support of Surface is completed
      libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.11" % Test,
      libraryDependencies += "org.wvlet.airframe" %%% "airframe-log" % AIRFRAME_VERSION,
      libraryDependencies ++= surfaceDependencies(scalaVersion.value)
    )
    .jvmSettings(
      // For adding PreDestroy, PostConstruct annotations to Java9
      libraryDependencies ++= surfaceJVMDependencies(scalaVersion.value),
      libraryDependencies += "javax.annotation" % "javax.annotation-api" % JAVAX_ANNOTATION_API_VERSION % Test
    )
    .jsSettings(jsBuildSettings)

lazy val root = project.in(file(".")).aggregate(surface.jvm, surface.js).settings(
  name := "light-surface",
  tokenSettings,
  crossScalaVersions := targetScalaVersions
)

