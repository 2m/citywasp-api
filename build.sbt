organization := "citywasp"
name := "citywasp-api"

val Akka = "2.6.8"
val AkkaHttp = "10.2.0"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"     % Akka,
  "com.typesafe.akka" %% "akka-http"       % AkkaHttp,
  "de.heikoseeberger" %% "akka-http-circe" % "1.34.0",
  "org.scalatest"     %% "scalatest"       % "3.2.2" % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtOnCompile in ThisBuild := true
