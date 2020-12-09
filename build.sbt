organization := "citywasp"
name := "citywasp-api"

val Akka = "2.6.10"
val AkkaHttp = "10.2.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"     % Akka,
  "com.typesafe.akka" %% "akka-http"       % AkkaHttp,
  "de.heikoseeberger" %% "akka-http-circe" % "1.35.2",
  "org.scalatest"     %% "scalatest"       % "3.2.3" % "test"
)

licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))

scalafmtOnCompile in ThisBuild := true
