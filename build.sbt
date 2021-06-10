organization := "citywasp"
name := "citywasp-api"

scalaVersion := "2.13.6"

val Akka = "2.6.15"
val AkkaHttp = "10.2.4"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-stream"     % Akka,
  "com.typesafe.akka" %% "akka-http"       % AkkaHttp,
  "de.heikoseeberger" %% "akka-http-circe" % "1.36.0",
  "org.scalatest"     %% "scalatest"       % "3.2.9" % "test"
)

scalafmtOnCompile := true
scalafixOnCompile := true

ThisBuild / scalafixDependencies ++= Seq(
  "com.nequissimus" %% "sort-imports" % "0.5.5"
)

enablePlugins(AutomateHeaderPlugin)
startYear := Some(2015)
organizationName := "github.com/2m/citywasp-api/contributors"
licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
