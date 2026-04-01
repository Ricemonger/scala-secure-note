package com.github.ricemonger.secretnote.http.routes

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.syntax.all.*
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException, UserNotFoundException}
import com.github.ricemonger.secretnote.logging.syntax.*
import io.circe.{CursorOp, DecodingFailure}
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

object GlobalRoutesErrorHandler {

  def apply[F[_] : {MonadThrow, Logger}](routes: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    Kleisli { req =>
      OptionT(
        routes.run(req).value.logError {
          case _: InvalidUserCredentialsException   => s"Auth failed: Invalid credentials provided for ${req.uri}"
          case UserAlreadyExistsException(username) => s"Registration failed: User $username already exists"
          case UserNotFoundException(username)      => s"Not found: User $username not found"
          case e: MessageFailure                    => s"Bad request on ${req.method} ${req.uri}: ${extractMessageFailureCause(e)}"
          case e                                    => s"Unexpected error on ${req.method} ${req.uri}: ${e.getMessage}"
        }
      ).handleErrorWith {
        case _: InvalidUserCredentialsException   => OptionT.liftF(Forbidden("Invalid Credentials"))
        case UserAlreadyExistsException(username) => OptionT.liftF(Conflict(s"User $username already exists"))
        case UserNotFoundException(username)      => OptionT.liftF(NotFound(s"User $username not found"))
        case e: MessageFailure                    => OptionT.liftF(BadRequest(s"Invalid input: ${extractMessageFailureCause(e)}"))
        case _                                    => OptionT.liftF(InternalServerError("An unexpected error occurred"))
      }
    }
  }

  private def extractMessageFailureCause(e: MessageFailure): String = e.getCause match {
    case df: DecodingFailure =>
      val path = CursorOp.opsToPath(df.history)
      s"Missing or invalid field at path '$path'"
    case cause if cause != null =>
      cause.getMessage
    case _ =>
      e.getMessage()
  }
}