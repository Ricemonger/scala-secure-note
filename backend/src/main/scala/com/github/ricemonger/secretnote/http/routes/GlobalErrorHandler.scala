package com.github.ricemonger.secretnote.http.routes

import cats.MonadThrow
import cats.data.{Kleisli, OptionT}
import cats.syntax.all.*
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException, UserNotFoundException}
import org.http4s.*
import org.http4s.dsl.Http4sDsl

object GlobalErrorHandler {

  def apply[F[_] : MonadThrow](routes: HttpRoutes[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F] {}
    import dsl.*

    Kleisli { req =>
      routes.run(req).handleErrorWith {
        case _: InvalidUserCredentialsException => OptionT.liftF(Forbidden("Invalid Credentials"))

        case UserAlreadyExistsException(username) => OptionT.liftF(Conflict(s"User $username already exists"))

        case UserNotFoundException(username) => OptionT.liftF(NotFound(s"User $username not found"))

        case e => OptionT.liftF(InternalServerError("An unexpected error occurred"))
      }
    }
  }
}
