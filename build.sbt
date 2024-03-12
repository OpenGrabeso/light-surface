import scalajsbundler.JSDOMNodeJSEnv

val SCALA_2_12          = "2.12.19"
val SCALA_2_13          = "2.13.13"
val SCALA_3             = "3.3.3"
val uptoScala2          = SCALA_2_13 :: SCALA_2_12 :: Nil
val targetScalaVersions = SCALA_3 :: uptoScala2

// Add this for using snapshot versions
ThisBuild / resolvers ++= Resolver.sonatypeOssRepos("snapshots")

val AIRSPEC_VERSION                 = sys.env.getOrElse("AIRSPEC_VERSION", "24.2.3")
val SCALACHECK_VERSION              = "1.17.0"
val JS_JAVA_LOGGING_VERSION         = "1.0.0"
val JAVAX_ANNOTATION_API_VERSION    = "1.3.2"

// [Development purpose] publish all artifacts to the local repo
addCommandAlias(
  "publishAllLocal",
  s"+ projectJVM/publishLocal; + projectJS/publishLocal;"
)

addCommandAlias(
  "publishJSLocal",
  s"+ projectJS/publishLocal"
)

// Allow using Ctrl+C in sbt without exiting the prompt
// Global / cancelable := true

//ThisBuild / turbo := true

// Reload build.sbt on changes
Global / onChangedBuildSource := ReloadOnSourceChanges

// ideSkipProject is used only for IntelliJ IDEA
Global / excludeLintKeys ++= Set(ideSkipProject)

// Disable the pipelining available since sbt-1.4.0. It caused compilation failure
ThisBuild / usePipelining := false

// Use Scala 3 by default as scala-2 specific source code is relatively small now
ThisBuild / scalaVersion := SCALA_3

ThisBuild / organization := "org.opengrabeso"

val buildSettings = Seq[Setting[_]](
  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html")),
  // Exclude compile-time only projects. This is a workaround for bloop,
  // which cannot resolve Optional dependencies nor compile-internal dependencies.
  crossScalaVersions    := targetScalaVersions,
  crossPaths            := true,
  publishMavenStyle     := true,
  javacOptions ++= Seq("-source", "11", "-target", "11"),
  scalacOptions ++= Seq(
    "-feature",
    "-deprecation"
    // Use this flag for debugging Macros
    // "-Xcheck-macros",
  ) ++ {
    if (scalaVersion.value.startsWith("3.")) {
      Seq.empty
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
    "org.wvlet.airframe" %%% "airspec"    % AIRSPEC_VERSION    % Test,
    "org.scalacheck"     %%% "scalacheck" % SCALACHECK_VERSION % Test
  ) ++ {
    if (scalaVersion.value.startsWith("3."))
      Seq.empty
    else
      Seq("org.scala-lang.modules" %%% "scala-collection-compat" % "2.11.0")
  }
)

val scala2Only = Seq[Setting[_]](
  scalaVersion       := SCALA_2_13,
  crossScalaVersions := uptoScala2
)

val scala3Only = Seq[Setting[_]](
  scalaVersion       := SCALA_3,
  crossScalaVersions := List(SCALA_3)
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
  coverageEnabled := false
)

val noPublish = Seq(
  publishArtifact := false,
  publish         := {},
  publishLocal    := {},
  publish / skip  := true,
  // This must be Nil to use crossScalaVersions of individual modules in `+ projectJVM/xxxx` tasks
  crossScalaVersions := Nil,
  // Explicitly skip the doc task because protobuf related Java files causes no type found error
  Compile / doc / sources                := Seq.empty,
  Compile / packageDoc / publishArtifact := false,
)

lazy val root =
  project
    .in(file("."))
    .settings(name := "airframe-root")
    .settings(buildSettings)
    .settings(noPublish)
    .aggregate((jvmProjects ++ jsProjects): _*)

// JVM projects for scala-community build. This should have no tricky setup and should support Scala 2.12 and Scala 3
lazy val communityBuildProjects: Seq[ProjectReference] = Seq(
  log.jvm,
  surface.jvm,
)

// Other JVM projects supporting Scala 2.12 - Scala 2.13
lazy val jvmProjects: Seq[ProjectReference] = communityBuildProjects

// Scala.js build (Scala 2.12, 2.13, and 3.x)
lazy val jsProjects: Seq[ProjectReference] = Seq(
  log.js,
  surface.js,
)

// For Scala 2.12
lazy val projectJVM =
  project
    .settings(noPublish)
    .settings(
      // Skip importing aggregated projects in IntelliJ IDEA
      ideSkipProject := true,
      // Use a stable coverage directory name without containing scala version
      coverageDataDir := target.value
    )
    .aggregate(jvmProjects: _*)

lazy val projectJS =
  project
    .settings(noPublish)
    .settings(
      // Skip importing aggregated projects in IntelliJ IDEA
      ideSkipProject := true
    )
    .aggregate(jsProjects: _*)

// A scoped project only for Dotty (Scala 3).
// This is a workaround as projectJVM/test shows compile errors for non Scala 3 ready projects
lazy val projectDotty =
  project
    .settings(noPublish)
    .settings(
      // Skip importing aggregated projects in IntelliJ IDEA
      ideSkipProject := true
    )
    .aggregate(
      log.jvm,
      surface.jvm,
    )

def parallelCollection(scalaVersion: String) = {
  if (scalaVersion.startsWith("2.13.")) {
    Seq("org.scala-lang.modules" %% "scala-parallel-collections" % "0.2.0")
  } else {
    Seq.empty
  }
}

// // To use airframe in other airframe modules, we need to reference airframeMacros project
// lazy val airframeMacrosJVMRef = airframeMacrosJVM % Optional
// lazy val airframeMacrosRef    = airframeMacros    % Optional
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
      name        := "airframe-surface",
      description := "A library for extracting object structure surface",
      // TODO: This is a temporary solution. Use AirSpec after Scala 3 support of Surface is completed
      libraryDependencies += "org.scalameta" %%% "munit" % "0.7.29" % Test,
      libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.11" % Test,
      libraryDependencies ++= surfaceDependencies(scalaVersion.value)
    )
    .jvmSettings(
      // For adding PreDestroy, PostConstruct annotations to Java9
      libraryDependencies ++= surfaceJVMDependencies(scalaVersion.value),
      libraryDependencies += "javax.annotation" % "javax.annotation-api" % JAVAX_ANNOTATION_API_VERSION % Test
    )
    .jsSettings(jsBuildSettings)
    .dependsOn(log)


val logDependencies = { scalaVersion: String =>
  scalaVersion match {
    case s if s.startsWith("3.") =>
      Seq.empty
    case _ =>
      Seq("org.scala-lang" % "scala-reflect" % scalaVersion % Provided)
  }
}

val logJVMDependencies = Seq(
  // For rotating log files
  "ch.qos.logback" % "logback-core" % "1.3.14"
)

// airframe-log should have minimum dependencies
lazy val log: sbtcrossproject.CrossProject =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("airframe-log"))
    .settings(buildSettings)
    .settings(
      name        := "airframe-log",
      description := "Fancy logger for Scala",
      scalacOptions ++= {
        if (scalaVersion.value.startsWith("3.")) Seq("-source:3.0-migration")
        else Nil
      },
      libraryDependencies ++= logDependencies(scalaVersion.value)
    )
    .jvmSettings(
      libraryDependencies ++= logJVMDependencies,
      runTestSequentially
    )
    .jsSettings(
      jsBuildSettings,
      libraryDependencies ++= Seq(
        ("org.scala-js" %%% "scalajs-java-logging" % JS_JAVA_LOGGING_VERSION).cross(CrossVersion.for3Use2_13)
      )
    )

// Workaround for com.twitter:util-core_2.12:21.4.0 (depends on 1.1.2)
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-parser-combinators" % "always"
