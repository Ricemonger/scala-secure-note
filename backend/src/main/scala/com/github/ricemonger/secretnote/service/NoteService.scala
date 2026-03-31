package com.github.ricemonger.secretnote.service

import cats.MonadThrow
import cats.syntax.all.*
import com.github.ricemonger.secretnote.domain.user.User
import com.github.ricemonger.secretnote.exception.UserNotFound
import com.github.ricemonger.secretnote.repository.UserRepository.UserRepository

import java.util.UUID

object NoteService {

  trait NoteService[F[_]] {
    def getSecretNote(id: UUID): F[String]

    def updateSecretNote(id: UUID, newNote: String): F[User]
  }

  class LiveNoteService[F[_] : MonadThrow](repo: UserRepository[F]) extends NoteService[F] {

    def getSecretNote(id: UUID): F[String] = {
      repo.selectNoteById(id).flatMap {
        case Some(Some(note: String)) => note.pure[F]
        case Some(None) => "".pure[F]
        case None => UserNotFound(id.toString).raiseError[F, String]
      }
    }

    def updateSecretNote(id: UUID, newNote: String): F[User] = {
      repo.updateNoteById(id, newNote).flatMap {
        case Some(updatedUser) => updatedUser.pure[F]
        case None => UserNotFound(id.toString).raiseError[F, User]
      }
    }
  }
}