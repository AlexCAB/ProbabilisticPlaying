name := "Probabilistic Playing"

version := "0.1.0"


val jdkVers = "1.8"

lazy val commonSettings = Seq(
  scalaVersion := "2.11.8",
  javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVers, "-target", jdkVers, "-Xlint:deprecation", "-Xlint:unchecked"),
  scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVers", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")
)


lazy val mathact = (project in file("mathact"))
  .settings(commonSettings: _*)
  
lazy val simulation = (project in file("simulation"))
  .settings(commonSettings: _*)
  .dependsOn(mathact)
  .aggregate(mathact)

lazy val pacman = (project in file("pacman"))
  .settings(commonSettings: _*)
  .dependsOn(mathact, simulation)
  .aggregate(mathact, simulation)

lazy val root = (project in file(".")).
  aggregate(mathact, simulation, pacman)