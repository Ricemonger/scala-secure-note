package com.github.ricemonger.secretnote.http.routes

import cats.Monad
import cats.effect.Concurrent
import org.typelevel.log4cats.Logger
import cats.syntax.all.*
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.server.Router

class NotesRoutes[F[_] : Monad] private extends Http4sDsl[F] {

  private val getEndpoint: HttpRoutes[F] = HttpRoutes.of[F] { case GET -> Root =>
    Ok("note get")
  }

  private val putEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case PUT -> Root => Ok("note put")
  }

  val routes: HttpRoutes[F] = Router(
    "notes" -> (getEndpoint <+> putEndpoint)
  )
}

object NotesRoutes {
  def apply[F[_] : {Concurrent, Logger}] = new NotesRoutes[F]
}
