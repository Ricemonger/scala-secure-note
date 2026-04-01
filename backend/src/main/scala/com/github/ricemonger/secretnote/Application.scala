package com.github.ricemonger.secretnote

import cats.effect.*
import com.github.ricemonger.secretnote.config.syntax.loadF
import com.github.ricemonger.secretnote.config.{DatabaseConfig, EmberConfig, JwtConfig}
import com.github.ricemonger.secretnote.http.auth.JwtAuthMiddleware
import com.github.ricemonger.secretnote.http.routes.{AuthRoutes, GlobalRoutesErrorHandler, NotesRoutes}
import com.github.ricemonger.secretnote.repository.UserRepository.LiveUserRepository
import com.github.ricemonger.secretnote.service.AuthService.LiveAuthService
import com.github.ricemonger.secretnote.service.NoteService.LiveNoteService
import doobie.hikari.HikariTransactor
import org.flywaydb.core.Flyway
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.Router
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource

import java.util.TimeZone
import scala.concurrent.ExecutionContext

object Application extends IOApp.Simple {

  given Logger[IO] = Slf4jLogger.getLogger[IO]

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
  
  override def run: IO[Unit] = for {
    emberConfig <- ConfigSource.default.at("ember").loadF[IO, EmberConfig]
    jwtConfig <- ConfigSource.default.at("jwt").loadF[IO, JwtConfig]
    dbConfig <- ConfigSource.default.at("database").loadF[IO, DatabaseConfig]

    _ <- runFlywayMigrations(dbConfig)

    _ <- (for {
      xa <- HikariTransactor.newHikariTransactor[IO](
        dbConfig.driver,
        dbConfig.url,
        dbConfig.user,
        dbConfig.password,
        ExecutionContext.global
      )

      userRepo = new LiveUserRepository[IO](xa)
      authService = new LiveAuthService[IO](userRepo, jwtConfig)
      noteService = new LiveNoteService[IO](userRepo)

      authRoutes = AuthRoutes[IO](authService)
      notesRoutes = NotesRoutes[IO](noteService)
      authMiddleware = JwtAuthMiddleware[IO](jwtConfig)
      securedNotesRoutes = authMiddleware(notesRoutes.authedRoutes)

      httpApp = Router(
        "" -> GlobalRoutesErrorHandler(authRoutes.routes),
        "/notes" -> GlobalRoutesErrorHandler(securedNotesRoutes)
      ).orNotFound

      server <- EmberServerBuilder
        .default[IO]
        .withHost(emberConfig.host)
        .withPort(emberConfig.port)
        .withHttpApp(httpApp)
        .build

    } yield server).use { _ =>
      IO.println(s"Server ready on ${emberConfig.host}:${emberConfig.port}!") *> IO.never
    }
  } yield ()

  private def runFlywayMigrations(config: DatabaseConfig): IO[Unit] = IO.blocking {
    Flyway
      .configure()
      .dataSource(config.url, config.user, config.password)
      .load()
      .migrate()
  }
}