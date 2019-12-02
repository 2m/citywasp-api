organization := "citywasp"
name := "citywasp-api"

val Akka = "2.6.0"
val AkkaHttp = "10.1.11"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"     % Akka,
  "com.typesafe.akka" %% "akka-http"       % AkkaHttp,
  "de.heikoseeberger" %% "akka-http-circe" % "1.29.1",
  "org.scalatest"     %% "scalatest"       % "3.1.0" % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtOnCompile in ThisBuild := true
