organization := "citywasp"
name := "citywasp-api"

val Akka = "2.5.25"
val AkkaHttp = "10.1.9"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"     % Akka,
  "com.typesafe.akka" %% "akka-http"       % AkkaHttp,
  "de.heikoseeberger" %% "akka-http-circe" % "1.28.0",
  "org.scalatest"     %% "scalatest"       % "3.0.8" % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtOnCompile in ThisBuild := true
