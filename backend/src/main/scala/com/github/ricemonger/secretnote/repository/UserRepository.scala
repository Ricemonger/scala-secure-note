package com.github.ricemonger.secretnote.repository

import cats.effect.Async
import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials}
import doobie.Transactor
import doobie.implicits.*
import doobie.postgres.implicits.*

import java.util.UUID

object UserRepository {

  trait UserRepository[F[_]] {
    def selectNoteById(id: UUID): F[Option[Option[String]]]

    def selectCredentialsByUsername(username: String): F[Option[UserCredentials]]

    def insert(userCredentials: UserCredentials): F[Option[User]]

    def updateNoteById(id: UUID, secretNote: String): F[Option[User]]
  }

  class LiveUserRepository[F[_] : Async](xa: Transactor[F]) extends UserRepository[F] {

    def selectNoteById(id: UUID): F[Option[Option[String]]] =
      sql"SELECT secret_note FROM users WHERE id = $id"
        .query[Option[String]] 
        .option                
        .transact(xa)

    def selectCredentialsByUsername(username: String): F[Option[UserCredentials]] = {
      sql"SELECT username, password_hash FROM users WHERE username = $username"
        .query[UserCredentialsProjection]
        .map(_.toDomain)
        .option
        .transact(xa)
    }

    def insert(userCredentials: UserCredentials): F[Option[User]] = {
      val proj = UserCredentialsProjection.fromDomain(userCredentials)
      sql"""
          INSERT INTO users (id, username, password_hash)
          VALUES (gen_random_uuid(), ${proj.username}, ${proj.password_hash})
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