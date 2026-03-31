package com.github.ricemonger.secretnote.service

import cats.MonadThrow
import cats.syntax.all.*
import com.github.ricemonger.secretnote.domain.user.UserCredentials
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException}
import com.github.ricemonger.secretnote.repository.UserRepository.UserRepository

object AuthService {

  trait AuthService[F[_]] {
    def register(username: String, passwordAttempt: String): F[String]

    def login(username: String, passwordAttempt: String): F[String]
  }

  class LiveAuthService[F[_] : MonadThrow](repo: UserRepository[F]) extends AuthService[F] {

    def register(username: String, passwordAttempt: String): F[String] = {

      val passwordHash = passwordAttempt

      repo.insertCredentials(UserCredentials(username, passwordHash)).flatMap {
        case Some(user) => "jwt".pure[F]
        case None => UserAlreadyExistsException(username).raiseError[F, String]
      }
    }

    def login(username: String, passwordAttempt: String): F[String] = repo.selectCredentialsByUsername(username).flatMap {
      case Some(credentials) if credentials.passwordHash == passwordAttempt => "jwt".pure[F]
      case _ => InvalidUserCredentialsException().raiseError[F, String]
    }
  }
}
