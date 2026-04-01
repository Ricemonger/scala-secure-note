package com.github.ricemonger.secretnote.http.routes

import cats.effect.IO
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException}
import com.github.ricemonger.secretnote.service.AuthService.AuthService
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

class AuthRoutesTest extends CatsEffectSuite {
  
  implicit val logger: Logger[IO] = NoOpLogger[IO]

  class FakeAuthService(
                         registerResult: IO[String] = IO.pure("fake-jwt"),
                         loginResult: IO[String] = IO.pure("fake-jwt")
                       ) extends AuthService[IO] {
    override def register(username: String, passwordAttempt: String): IO[String] = registerResult
    override def login(username: String, passwordAttempt: String): IO[String] = loginResult
  }

  test("POST /register returns 200 OK and a JWT on success") {
    val service = new FakeAuthService(registerResult = IO.pure("valid-token"))
    val routes = AuthRoutes[IO](service).routes
    val req = Request[IO](Method.POST, uri"/register").withEntity(AuthPayload("testUser", "password"))

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.as[JwtResponse]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.jwt, "valid-token")
    }
  }

  test("POST /register mapped by GlobalErrorHandler returns 409 Conflict if user exists") {
    val service = new FakeAuthService(registerResult = IO.raiseError(UserAlreadyExistsException("testUser")))
    val routes = GlobalErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/register").withEntity(AuthPayload("testUser", "password"))

    for {
      resp <- routes.orNotFound.run(req)
    } yield {
      assertEquals(resp.status, Status.Conflict)
    }
  }

  test("POST /login returns 200 OK and a JWT on valid credentials") {
    val service = new FakeAuthService(loginResult = IO.pure("login-token"))
    val routes = AuthRoutes[IO](service).routes
    val req = Request[IO](Method.POST, uri"/login").withEntity(AuthPayload("testUser", "password"))

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.as[JwtResponse]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.jwt, "login-token")
    }
  }

  test("POST /login mapped by GlobalErrorHandler returns 403 Forbidden on invalid credentials") {
    val service = new FakeAuthService(loginResult = IO.raiseError(InvalidUserCredentialsException()))
    val routes = GlobalErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/login").withEntity(AuthPayload("testUser", "wrongPass"))

    for {
      resp <- routes.orNotFound.run(req)
    } yield {
      assertEquals(resp.status, Status.Forbidden)
    }
  }
}