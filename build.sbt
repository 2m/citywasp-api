val Tapir = "1.11.7"

lazy val citywasp = project
  .in(file("."))
  .settings(sonatypeProfileName := "lt.dvim")
  .aggregate(api, cli)

lazy val api = (project in file("api"))
  .settings(
    name := "citywasp-api",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.tapir" %% "tapir-core"           % Tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-json-circe"     % Tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-sttp-client"    % Tapir,
      "com.softwaremill.sttp.tapir" %% "tapir-refined"        % Tapir,
      "io.circe"                    %% "circe-generic"        % "0.14.10",
      "io.circe"                    %% "circe-generic-extras" % "0.14.4",
      "org.scalameta"               %% "munit"                % "1.0.2" % Test
    )
  )
  .enablePlugins(AutomateHeaderPlugin)

lazy val cli = (project in file("cli"))
  .settings(
    name := "citywasp-cli",
    libraryDependencies ++= Seq(
      "com.monovore"                  %% "decline"             % "2.4.1",
      "com.monovore"                  %% "decline-refined"     % "2.4.1",
      "org.typelevel"                 %% "cats-effect"         % "3.5.5",
      "com.softwaremill.sttp.client3" %% "http4s-backend"      % "3.10.1",
      "org.http4s"                    %% "http4s-blaze-client" % "0.23.16"
    )
  )
  .dependsOn(api)
  .enablePlugins(AutomateHeaderPlugin, JavaAppPackaging)

inThisBuild(
  Seq(
    organization := "lt.dvim.citywasp",
    scalaVersion := "2.13.10",
    scalacOptions += "-Ymacro-annotations",
    scalafmtOnCompile := true,
    scalafixOnCompile := true,
    scalafixDependencies ++= Seq(
      "com.nequissimus" %% "sort-imports" % "0.6.1"
    ),
    startYear := Some(2015),
    organizationName := "github.com/2m/citywasp-api/graphs/contributors",
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    homepage := Some(url("https://github.com/2m/citywasp-api")),
    developers := List(
      Developer(
        "contributors",
        "Contributors",
        "https://gitter.im/2m/general",
        url("https://github.com/2m/citywasp-api/graphs/contributors")
      )
    ),
    versionScheme := Some("semver-spec")
  )
)
