package com.github.ricemonger.secretnote.http.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.github.ricemonger.secretnote.config.ConstraintConfig
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException}
import com.github.ricemonger.secretnote.service.AuthService.AuthService
import io.circe.{Decoder, DecodingFailure, HCursor}
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.typelevel.log4cats.Logger

case class AuthPayload(username: String, password: String)
case class JwtResponse(jwt: String)

class AuthRoutes[F[_] : Concurrent] private(authService: AuthService[F], constraintConfig: ConstraintConfig) extends Http4sDsl[F] {

  given Decoder[AuthPayload] = (c: HCursor) => for {
    username <- c.downField("username").as[String]
    _ <- Either.cond(
      username.matches(constraintConfig.usernameRegex),
      (),
      DecodingFailure(constraintConfig.usernameMessage, c.downField("username").history)
    )

    password <- c.downField("password").as[String]
    _ <- Either.cond(
      password.matches(constraintConfig.passwordRegex),
      (),
      DecodingFailure(constraintConfig.passwordMessage, c.downField("password").history)
    )
  } yield AuthPayload(username, password)

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
  def apply[F[_] : {Concurrent, Logger}](authService: AuthService[F], constraintConfig: ConstraintConfig) =
    new AuthRoutes[F](authService, constraintConfig)
}