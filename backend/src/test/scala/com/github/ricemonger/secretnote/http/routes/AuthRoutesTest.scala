package com.github.ricemonger.secretnote.http.routes

import cats.effect.IO
import com.github.ricemonger.secretnote.service.AuthService
import io.circe.Json
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.*
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
    val req = Request[IO](Method.POST, uri"/register").withEntity(
      Json.obj(
        "username" -> Json.fromString("testUser"),
        "password" -> Json.fromString("password")
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.as[JwtResponse]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.jwt, "valid-token")
    }
  }

  test("POST /register rejects invalid username by constraints") {
    val service = new FakeAuthService()
    val routes = GlobalRoutesErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/register").withEntity(
      Json.obj(
        "username" -> Json.fromString("inv"),
        "password" -> Json.fromString("password")
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.bodyText.compile.string
    } yield {
      assertEquals(resp.status, Status.BadRequest)
      assertEquals(body, "Invalid input: Field at path '.username' is invalid: Username must be between 4 and 20 characters long and can only contain English letters, numbers and ._-")
    }
  }

  test("POST /register rejects invalid password by constraints") {
    val service = new FakeAuthService()
    val routes = GlobalRoutesErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/register").withEntity(
      Json.obj(
        "username" -> Json.fromString("valid"),
        "password" -> Json.fromString("inv") 
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.bodyText.compile.string
    } yield {
      assertEquals(resp.status, Status.BadRequest)
      assertEquals(body, "Invalid input: Field at path '.password' is invalid: Password must be between 4 and 20 characters long and can contain English letters, numbers and ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : \" \\ | , . < > / ?")
    }
  }

  test("POST /login returns 200 OK and a JWT on valid credentials") {
    val service = new FakeAuthService(loginResult = IO.pure("login-token"))
    val routes = AuthRoutes[IO](service).routes
    val req = Request[IO](Method.POST, uri"/login").withEntity(
      Json.obj(
        "username" -> Json.fromString("testUser"),
        "password" -> Json.fromString("password")
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.as[JwtResponse]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.jwt, "login-token")
    }
  }

  test("POST /login rejects invalid username by constraints") {
    val service = new FakeAuthService()
    val routes = GlobalRoutesErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/login").withEntity(
      Json.obj(
        "username" -> Json.fromString("inv"),
        "password" -> Json.fromString("password")
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.bodyText.compile.string
    } yield {
      assertEquals(resp.status, Status.BadRequest)
      assertEquals(body, "Invalid input: Field at path '.username' is invalid: Username must be between 4 and 20 characters long and can only contain English letters, numbers and ._-")
    }
  }

  test("POST /login rejects invalid password by constraints") {
    val service = new FakeAuthService()
    val routes = GlobalRoutesErrorHandler[IO](AuthRoutes[IO](service).routes)
    val req = Request[IO](Method.POST, uri"/login").withEntity(
      Json.obj(
        "username" -> Json.fromString("valid"),
        "password" -> Json.fromString("inv")
      )
    )

    for {
      resp <- routes.orNotFound.run(req)
      body <- resp.bodyText.compile.string
    } yield {
      assertEquals(resp.status, Status.BadRequest)
      assertEquals(body, "Invalid input: Field at path '.password' is invalid: Password must be between 4 and 20 characters long and can contain English letters, numbers and ! @ # $ % ^ & * ( ) _ + - = [ ] { } ; ' : \" \\ | , . < > / ?")
    }
  }
}