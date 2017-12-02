lazy val root = (project in file("."))
  .settings(
    name         := "solarweb-interface",
    organization := "de.softwareschmied",
    scalaVersion := "2.12.4",
    version      := "0.0.1-SNAPSHOT"
  )

val akkaHttpVersion = "10.0.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "io.spray" %% "spray-json" % "1.3.3"