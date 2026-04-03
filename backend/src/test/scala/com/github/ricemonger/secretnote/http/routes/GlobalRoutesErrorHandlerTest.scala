package com.github.ricemonger.secretnote.http.routes

import cats.effect.IO
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException, UserNotFoundException}
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.implicits.uri
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

class GlobalRoutesErrorHandlerTest extends CatsEffectSuite {

  implicit val logger: Logger[IO] = NoOpLogger[IO]

  def failingRoute(ex: Throwable): HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root => IO.raiseError(ex)
  }

  test("maps InvalidUserCredentialsException to 403 Forbidden") {
    val route = GlobalRoutesErrorHandler[IO](failingRoute(InvalidUserCredentialsException()))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Forbidden)
  }

  test("maps UserAlreadyExistsException to 409 Conflict") {
    val route = GlobalRoutesErrorHandler[IO](failingRoute(UserAlreadyExistsException("testUser")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Conflict)
  }

  test("maps UserNotFoundException to 404 NotFound") {
    val route = GlobalRoutesErrorHandler[IO](failingRoute(UserNotFoundException("testUser")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.NotFound)
  }

  test("maps MessageFailure with DecodingFailure cause to formatted 400 BadRequest") {
    import io.circe.{CursorOp, DecodingFailure}

    val decodingFailure = DecodingFailure("Missing field", List(CursorOp.DownField("secretNote")))
    val messageFailure = InvalidMessageBodyFailure("Could not decode JSON", Some(decodingFailure))

    val route = GlobalRoutesErrorHandler[IO](failingRoute(messageFailure))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.BadRequest)
      assertEquals(body, "Invalid input: Field at path '.secretNote' is invalid: Missing field")
    }
  }

  test("maps unhandled exceptions to 500 InternalServerError") {
    val route = GlobalRoutesErrorHandler[IO](failingRoute(new RuntimeException("Boom!")))
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- route.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.InternalServerError)
  }
}