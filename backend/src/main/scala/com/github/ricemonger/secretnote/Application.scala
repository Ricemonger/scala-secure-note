package com.github.ricemonger.secretnote

import cats.effect.*
import cats.syntax.all.* 
import com.github.ricemonger.secretnote.config.EmberConfig
import com.github.ricemonger.secretnote.config.syntax.loadF
import com.github.ricemonger.secretnote.http.routes.{AuthRoutes, NotesRoutes}
import org.http4s.ember.server.EmberServerBuilder
import pureconfig.ConfigSource

object Application extends IOApp.Simple {

  override def run: IO[Unit] = ConfigSource.default.loadF[IO, EmberConfig].flatMap { config =>
    
    val combinedRoutes = (AuthRoutes[IO].routes <+> NotesRoutes[IO].routes).orNotFound

    EmberServerBuilder
      .default[IO]
      .withHost(config.host)
      .withPort(config.port)
      .withHttpApp(combinedRoutes) 
      .build
      .use(_ => IO.println(s"Server ready on ${config.host}:${config.port}!") *> IO.never)
  }
}