name := "crawler4j"
organization := "edu.uci.ics"
version := "4.3-SNAPSHOT"
publishMavenStyle := true
autoScalaLibrary := false
crossPaths := false

javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.10"
libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.4"
libraryDependencies += "com.sleepycat" % "je" % "5.0.73"
libraryDependencies += "org.apache.tika" % "tika-parsers" % "1.5"
libraryDependencies += "uk.org.lidalia" % "lidalia-slf4j-ext" % "1.0.0"

libraryDependencies += "junit" % "junit" % "4.11" % "test"
