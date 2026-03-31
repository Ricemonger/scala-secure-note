package com.github.ricemonger.secretnote.service

import cats.MonadThrow
import cats.syntax.all.*
import com.github.ricemonger.secretnote.domain.user.User
import com.github.ricemonger.secretnote.exception.UserNotFoundException
import com.github.ricemonger.secretnote.repository.UserRepository.UserRepository

import java.util.UUID

object NoteService {

  trait NoteService[F[_]] {
    def updateSecretNote(id: UUID, note: String): F[User]
    
    def getSecretNote(id: UUID): F[String]
  }

  class LiveNoteService[F[_] : MonadThrow](repo: UserRepository[F]) extends NoteService[F] {

    def updateSecretNote(id: UUID, note: String): F[User] = repo.updateNoteById(id, note).flatMap {
      case Some(updatedUser) => updatedUser.pure[F]
      case None => UserNotFoundException(id.toString).raiseError[F, User]
    }

    def getSecretNote(id: UUID): F[String] = repo.selectNoteById(id).flatMap {
        case Some(Some(note: String)) => note.pure[F]
        case Some(None) => "".pure[F]
        case None => UserNotFoundException(id.toString).raiseError[F, String]
    }
  }
}