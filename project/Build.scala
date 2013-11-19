import sbt._
import Keys._

object Build extends Build {

  lazy val dummySystem: Project = (
    Project("dummy-system", file("."))
    settings(
      scalaVersion := "2.10.0",
      javacOptions ++= Seq("-source", "1.7"),
      scalacOptions in Test ++= Seq("-Yrangepos", "-feature"),
      resolvers += "spray" at "http://repo.spray.io/",
      libraryDependencies ++= Seq(
        "com.typesafe" % "config" % "1.0.2",
        "edu.gatech.gtri.typesafeconfig-extensions" % "typesafeconfig-factory" % "1.1",
        "javax.servlet" % "servlet-api" % "2.5",
        "org.eclipse.jetty" % "jetty-webapp" % "7.3.0.v20110203",
        "org.scalaj" % "scalaj-http_2.10" % "0.3.10",
        "com.typesafe.akka" %% "akka-actor" % "2.2.3",
        "net.liftweb" %%  "lift-json" % "2.5.1",
        "org.specs2" % "specs2_2.10" % "2.2.1" % "test"
      )
    )
  )
}
