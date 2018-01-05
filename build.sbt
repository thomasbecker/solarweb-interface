name := "solarweb-interface"
organization := "de.softwareschmied"
scalaVersion := "2.12.4"
version := "0.0.1-SNAPSHOT"

val akkaHttpVersion = "10.0.4"
libraryDependencies += "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion
libraryDependencies += "io.spray" %% "spray-json" % "1.3.3"
libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
libraryDependencies ++= Seq("org.specs2" %% "specs2-core" % "4.0.0" % "test")
