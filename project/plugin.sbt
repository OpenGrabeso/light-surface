addSbtPlugin("org.scalameta"       % "sbt-scalafmt"             % "2.5.2")
addSbtPlugin("org.portable-scala"  % "sbt-scalajs-crossproject" % "1.3.2")

// For auto-code rewrite
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.11.1")

// For Scala.js
val SCALAJS_VERSION = sys.env.getOrElse("SCALAJS_VERSION", "1.15.0")
addSbtPlugin("org.scala-js"  % "sbt-scalajs"         % SCALAJS_VERSION)

scalacOptions ++= Seq("-deprecation", "-feature")
