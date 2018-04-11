organization := "citywasp"
name := "citywasp-api"

val AkkaHttp = "10.1.1"

libraryDependencies ++= Seq(
  "net.databinder.dispatch" %% "dispatch-core" % "0.12.3",
  "com.typesafe"            % "config"         % "1.3.3",
  "org.scalatest"           %% "scalatest"     % "3.0.5"  % "test",
  "com.typesafe.akka"       %% "akka-stream"   % "2.5.11" % "test",
  "com.typesafe.akka"       %% "akka-http"     % AkkaHttp % "test",
  "com.typesafe.akka"       %% "akka-http-xml" % AkkaHttp % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtOnCompile in ThisBuild := true
