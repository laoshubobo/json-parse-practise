name := "jsparse-practise"

version := "0.1"

scalaVersion := "2.13.6"
libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.6.10",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "net.liftweb" %% "lift-json" % "3.4.1",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,
  "org.scalatest" %% "scalatest-funsuite" % "3.2.9" % "test"
)