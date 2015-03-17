import play.Project._

name := """stravoski"""

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	cache,
	javaJdbc,
	javaJpa,
	"org.hibernate" % "hibernate-entitymanager" % "3.6.9.Final",
	"org.webjars" %% "webjars-play" % "2.2.2", 
	"org.webjars" % "bootstrap" % "2.3.1")

playJavaSettings
