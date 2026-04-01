package com.github.ricemonger.secretnote.http.routes

import cats.effect.IO
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException, UserNotFoundException}
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

class GlobalErrorHandlerTest extends CatsEffectSuite {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  def failingRoute(ex: Throwable): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => IO.raiseError(ex)
  }

  test("maps InvalidUserCredentialsException to 403 Forbidden") {
    val route = GlobalErrorHandler[IO](failingRoute(InvalidUserCredentialsException()))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Forbidden)
  }

  test("maps UserAlreadyExistsException to 409 Conflict") {
    val route = GlobalErrorHandler[IO](failingRoute(UserAlreadyExistsException("testUser")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Conflict)
  }

  test("maps UserNotFoundException to 404 NotFound") {
    val route = GlobalErrorHandler[IO](failingRoute(UserNotFoundException("testUser")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.NotFound)
  }

  test("maps unhandled exceptions to 500 InternalServerError") {
    val route = GlobalErrorHandler[IO](failingRoute(new RuntimeException("Boom!")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.InternalServerError)
  }
}