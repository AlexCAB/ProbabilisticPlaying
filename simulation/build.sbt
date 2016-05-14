name := "Probabilistic Simulation"

version := "0.1.0"

scalaVersion := "2.11.8"

val jdkVersion = "1.8"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVersion, "-target", jdkVersion, "-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVersion", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")


libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.4",
  "org.scalatest"     %% "scalatest" % "2.2.1" % "test"
)