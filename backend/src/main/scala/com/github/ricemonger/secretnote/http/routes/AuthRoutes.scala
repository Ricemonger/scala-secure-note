package com.github.ricemonger.secretnote.http.routes

import cats.Monad
import cats.effect.Concurrent
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

class AuthRoutes[F[_] : Monad] private extends Http4sDsl[F] {
  
  private val registerEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok("register")
  }

  private val loginEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case POST -> Root =>
      Ok("login")
  }
  
  val routes: HttpRoutes[F] = Router(
    "/register" -> registerEndpoint,
    "/login"    -> loginEndpoint
  )
}

object AuthRoutes {
  def apply[F[_] : {Concurrent, Logger}] = new AuthRoutes[F]
}