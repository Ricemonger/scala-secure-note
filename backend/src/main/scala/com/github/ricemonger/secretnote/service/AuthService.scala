package com.github.ricemonger.secretnote.service

import cats.effect.Sync
import cats.syntax.all.*
import com.github.ricemonger.secretnote.config.JwtConfig
import com.github.ricemonger.secretnote.domain.user.UserCredentials
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException}
import com.github.ricemonger.secretnote.repository.UserRepository.UserRepository
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}
import tsec.common.Verified
import tsec.passwordhashers.PasswordHash
import tsec.passwordhashers.jca.BCrypt

import java.time.Instant
import java.util.UUID

object AuthService {

  trait AuthService[F[_]] {
    def register(username: String, passwordAttempt: String): F[String]
    def login(username: String, passwordAttempt: String): F[String]
  }

  class LiveAuthService[F[_] : Sync](repo: UserRepository[F], config: JwtConfig) extends AuthService[F] {

    private def generateJwt(id: UUID): F[String] = Sync[F].delay {
      val claim = JwtClaim(
        content = s"""{"id": "$id"}""",
        expiration = Some(Instant.now().plusSeconds(config.expiration).getEpochSecond),
        issuedAt = Some(Instant.now().getEpochSecond)
      )

      JwtCirce.encode(claim, config.secret, JwtAlgorithm.HS256)
    }

    def register(username: String, passwordAttempt: String): F[String] = {
      for {
        passwordHash <- BCrypt.hashpw[F](passwordAttempt)

        userOpt <- repo.insertCredentials(UserCredentials(username, passwordHash))

        jwt <- userOpt match {
          case Some(user) => generateJwt(user.id)
          case None => UserAlreadyExistsException(username).raiseError[F, String]
        }
      } yield jwt
    }

    def login(username: String, passwordAttempt: String): F[String] = {
      for {
        userOpt <- repo.selectByUsername(username)
        user <- userOpt.liftTo[F](InvalidUserCredentialsException())

        isValid <- BCrypt.checkpw[F](
          passwordAttempt,
          PasswordHash[BCrypt](user.passwordHash)
        )

        jwt <- if (isValid == Verified) generateJwt(user.id)
        else InvalidUserCredentialsException().raiseError[F, String]
      } yield jwt
    }
  }
}