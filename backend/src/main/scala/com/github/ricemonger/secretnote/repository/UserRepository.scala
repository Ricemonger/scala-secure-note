package com.github.ricemonger.secretnote.repository

import cats.effect.Async
import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials, UserInfo}
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.util.UUID

object UserRepository {

  trait UserRepository[F[_]] {
    def selectNoteById(id: UUID): F[Option[String]]

    def selectCredentialsByUsername(username: String): F[Option[UserCredentials]]

    def insert(userInfo: UserInfo): F[Option[User]]

    def updateNoteById(id: UUID, secretNote: String): F[Option[User]]
  }

  class LiveUserRepository[F[_] : Async](xa: Transactor[F]) extends UserRepository[F] {

    def selectNoteById(id: UUID): F[Option[String]] =
      sql"SELECT secret_note FROM users WHERE id = $id"
        .query[String]
        .option
        .transact(xa)

    def selectCredentialsByUsername(username: String): F[Option[UserCredentials]] = {
      sql"SELECT username, password_hash FROM users WHERE username = $username"
        .query[UserCredentialsProjection]
        .map(_.toDomain)
        .option
        .transact(xa)
    }

    def insert(userInfo: UserInfo): F[Option[User]] = {
      val proj = UserInfoProjection.fromDomain(userInfo)
      sql"""
          INSERT INTO users (id, username, password_hash, secret_note)
          VALUES (gen_random_uuid(), ${proj.username}, ${proj.password_hash}, ${proj.secret_note})
          RETURNING id, username, password_hash, secret_note
         """
        .query[UserEntity]
        .map(_.toDomain)
        .option
        .transact(xa)
    }

    def updateNoteById(id: UUID, secretNote: String): F[Option[User]] = {
      sql"""
          UPDATE users
          SET secret_note = $secretNote
          WHERE id = $id
          RETURNING id, username, password_hash, secret_note
        """
        .query[UserEntity]
        .map(_.toDomain)
        .option
        .transact(xa)
    }
  }
}