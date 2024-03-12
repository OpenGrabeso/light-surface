// Ignore binary incompatible errors for libraries using scala-xml.
// sbt-scoverage upgraded to scala-xml 2.1.0, but other sbt-plugins and Scala compilier 2.12 uses scala-xml 1.x.x
ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % "always"

addSbtPlugin("org.scoverage"       % "sbt-scoverage"            % "2.0.11")
addSbtPlugin("org.scalameta"       % "sbt-scalafmt"             % "2.5.2")
addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject" % "1.3.2")
addSbtPlugin("com.eed3si9n"        % "sbt-buildinfo"            % "0.11.0")
addSbtPlugin("org.jetbrains.scala" % "sbt-ide-settings"         % "1.1.2")

// For auto-code rewrite
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

addDependencyTreePlugin

// For Scala.js
val SCALAJS_VERSION = sys.env.getOrElse("SCALAJS_VERSION", "1.15.0")
addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % SCALAJS_VERSION)
addSbtPlugin("ch.epfl.scala" % "sbt-scalajs-bundler" % "0.21.1")
libraryDependencies ++= (
  Seq("org.scala-js" %% "scalajs-env-jsdom-nodejs" % "1.1.0")
)

scalacOptions ++= Seq("-deprecation", "-feature")
