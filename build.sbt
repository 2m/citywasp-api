organization := "citywasp"
name := "citywasp-api"

scalaVersion := "2.12.2"
val AkkaHttp = "10.0.7"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.2",
  "com.typesafe"            % "config"         % "1.3.0",
  "org.scalatest"           %% "scalatest"     % "3.0.3"  % Test,
  "com.typesafe.akka"       %% "akka-http"     % AkkaHttp % Test,
  "com.typesafe.akka"       %% "akka-http-xml" % AkkaHttp % Test
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtVersion := "1.0.0-RC2"
enablePlugins(ScalafmtPlugin)
