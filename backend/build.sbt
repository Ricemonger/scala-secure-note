ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.8.2"

lazy val catsEffectVersion = "3.7.0"
lazy val http4sVersion     = "0.23.33"
lazy val circeVersion      = "0.14.15"
lazy val doobieVersion     = "1.0.0-RC12"
lazy val jwtScalaVersion   = "11.0.3"
lazy val pureConfigVersion = "0.17.10"
lazy val munitVersion      = "1.2.4"
lazy val munitCatsEffectVersion = "2.2.0"
lazy val tsecVersion = "0.5.0"
lazy val logbackVersion = "1.5.32"
lazy val flywayVersion = "12.3.0"
lazy val testcontainersVersion = "0.44.1"

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

      "io.github.jmcardon" %% "tsec-password" % tsecVersion,

      "org.tpolecat" %% "doobie-core"     % doobieVersion,
      "org.tpolecat" %% "doobie-postgres" % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"   % doobieVersion,

      "org.flywaydb" % "flyway-core" % flywayVersion,

      "com.github.pureconfig" %% "pureconfig-generic-scala3" % pureConfigVersion,

      "ch.qos.logback" % "logback-classic" % logbackVersion,

      "org.scalameta" %% "munit"             % munitVersion % Test,
      "org.typelevel" %% "munit-cats-effect" % munitCatsEffectVersion % Test,

      "com.dimafeng" %% "testcontainers-scala-munit" % testcontainersVersion % Test,
      "com.dimafeng" %% "testcontainers-scala-postgresql" % testcontainersVersion % Test
    ),

    assembly / assemblyMergeStrategy := {
      case x if x.endsWith("module-info.class") => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    }
  )