name := "Probabilistic Playing"

version := "0.1.0"

lazy val mathact = project in file("mathact")

  
lazy val simulation = (project in file("simulation"))
  .dependsOn(mathact)
  .aggregate(mathact)

lazy val root = (project in file(".")).
  aggregate(mathact, simulation)