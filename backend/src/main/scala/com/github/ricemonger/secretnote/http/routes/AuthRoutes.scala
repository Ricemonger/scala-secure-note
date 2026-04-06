package com.github.ricemonger.secretnote.http.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.github.ricemonger.secretnote.service.AuthService
import io.circe.generic.auto.*
import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.circe.given
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

type UsernameConstraint = DescribedAs[
  Match["^[a-zA-Z0-9._-]{4,20}$"],
  "Username must be between 4 and 20 characters long and can only contain English letters, numbers and ._-"
]
type Username = String :| UsernameConstraint

type PasswordConstraint = DescribedAs[
  Match["^[a-zA-Z0-9!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]{4,20}$"],
  "Password must be between 4 and 20 characters long and can contain English letters, numbers and ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : \" \\ | , . < > / ?"
]
type Password = String :| PasswordConstraint

case class AuthPayload(username: Username, password: Password)
case class JwtResponse(jwt: String)

class AuthRoutes[F[_] : Concurrent] private(authService: AuthService[F]) extends Http4sDsl[F] {

  private val registerEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root => for {
      payload <- req.as[AuthPayload]
      jwt <- authService.register(payload.username, payload.password)
      resp <- Ok(JwtResponse(jwt))
    } yield resp
  }

  private val loginEndpoint: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root => for {
      payload <- req.as[AuthPayload]
      jwt <- authService.login(payload.username, payload.password)
      resp <- Ok(JwtResponse(jwt))
    } yield resp
  }

  val routes: HttpRoutes[F] = Router(
    "/register" -> registerEndpoint,
    "/login"    -> loginEndpoint
  )
}

object AuthRoutes {
  def apply[F[_] : {Concurrent, Logger}](authService: AuthService[F]) =
    new AuthRoutes[F](authService)
}