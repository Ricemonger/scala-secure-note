package com.github.ricemonger.secretnote.http.routes

import cats.Monad
import org.http4s.*
import org.http4s.dsl.*
import org.http4s.server.Router

class NotesRoutes[F[_] : Monad] private extends Http4sDsl[F] {

  private val noteEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case GET -> Root => Ok("note get")
    case PUT -> Root => Ok("note put")
  }

  val routes: HttpRoutes[F] = Router(
    "note" -> noteEndpoint
  )
}

object NotesRoutes {
  def apply[F[_] : Monad] = new NotesRoutes[F]
}
