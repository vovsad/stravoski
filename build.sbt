name := """stravoski"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "com.google.code.gson" % "gson" % "2.3.1",
  "com.google.guava" % "guava" % "18.0",
  "mysql" % "mysql-connector-java" % "5.1.35",
  "org.mockito" % "mockito-core" % "2.0.28-beta"
)

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator


fork in run := false

javaOptions in Test += "-Dconfig.file=conf/application.test.conf"