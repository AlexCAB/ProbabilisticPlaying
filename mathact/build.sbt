name := "MathAct"

version := "0.1"



val jdkVers = "1.8"

scalaVersion := "2.11.8"

javacOptions ++= Seq("-encoding", "UTF-8", "-source", jdkVers, "-target", jdkVers, "-Xlint:deprecation", "-Xlint:unchecked")

scalacOptions ++= Seq("-encoding", "UTF-8", s"-target:jvm-$jdkVers", "-feature", "-language:_", "-deprecation", "-unchecked", "-Xlint")



libraryDependencies  ++= Seq(
  "org.scalanlp" %% "breeze" % "0.10",
  "org.jfree" % "jfreechart" % "1.0.19",
  "org.scala-lang.modules" %% "scala-swing" % "1.0.1",
  "net.sf.jchart2d" % "jchart2d" % "3.3.2",
  "org.graphstream" % "gs-core" % "1.3",
  "org.graphstream" % "gs-algo" % "1.3",
  "org.graphstream" % "gs-ui" % "1.3"
)


