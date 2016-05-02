name := "Probabilistic Simulation"

version := "0.1.0"


val jdkVers = "1.8"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVers, "-target", jdkVers, "-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVers", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")



libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "2.2.1" % "test"
)