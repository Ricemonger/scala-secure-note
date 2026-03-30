ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.2"

lazy val catsEffectVersion = "3.7.0"
lazy val http4sVersion     = "0.23.32"
lazy val circeVersion      = "0.14.14"
lazy val doobieVersion     = "1.0.0-RC8"
lazy val jwtScalaVersion   = "11.0.3"
lazy val pureConfigVersion = "0.17.10"
lazy val munitVersion      = "1.2.4"

lazy val root = (project in file("."))
  .settings(
    name := "backend",
    libraryDependencies ++= Seq(

      "org.typelevel" %% "cats-effect" % catsEffectVersion,

      "org.http4s" %% "http4s-ember-server" % http4sVersion,
      "org.http4s" %% "http4s-ember-client" % http4sVersion,
      "org.http4s" %% "http4s-dsl"          % http4sVersion,
      "org.http4s" %% "http4s-circe"        % http4sVersion,

      "io.circe" %% "circe-core"    % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser"  % circeVersion,

      "com.github.jwt-scala" %% "jwt-circe" % jwtScalaVersion,

      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,

      "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigVersion,

      "org.scalameta" %% "munit"             % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % "2.0.0"      % Test
    )
  )