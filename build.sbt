organization := "citywasp"
name := "citywasp-api"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core"              % "0.11.2",
  "com.typesafe"            % "config"                      % "1.3.0",
  "org.scalatest"           %% "scalatest"                  % "2.2.5" % "test",
  "com.typesafe.akka"       %% "akka-http-experimental"     % "1.0" % "test",
  "com.typesafe.akka"       %% "akka-http-xml-experimental" % "1.0" % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

reformatOnCompileSettings
scalafmtConfig in ThisBuild := Some(file(".scalafmt"))
