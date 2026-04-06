package com.github.ricemonger.secretnote.service

import cats.effect.IO
import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials}
import com.github.ricemonger.secretnote.exception.UserNotFoundException
import com.github.ricemonger.secretnote.repository.UserRepository
import com.github.ricemonger.secretnote.service.LiveNoteService
import munit.CatsEffectSuite

import java.util.UUID

class NoteServiceTest extends CatsEffectSuite {

  val emptyNoteUUID = "123e4567-e89b-12d3-a456-426614174000"

  class FakeUserRepository(initialData: Map[UUID, User]) extends UserRepository[IO] {
    var data = initialData

    override def updateNoteById(id: UUID, secretNote: String): IO[Option[User]] = IO.pure {
      data.get(id).map { user =>
        val updatedUser = user.copy(secretNote = secretNote)
        data = data + (id -> updatedUser)
        updatedUser
      }
    }

    override def selectNoteById(id: UUID): IO[Option[Option[String]]] = IO.pure {
      if (id.toString == emptyNoteUUID) {
        data.get(id).map(user => Option.empty)
      }
      else data.get(id).map(user => Option(user.secretNote))
    }

    override def insertCredentials(uc: UserCredentials): IO[Option[User]] = IO.pure(None)

    override def selectByUsername(username: String): IO[Option[User]] = IO.pure(None)
  }

  test("updateSecretNote updates and returns updated user if user exists") {
    val userId = UUID.randomUUID()
    val oldUser = User(userId, "user", "passwordHash", "note")

    val fakeRepo = new FakeUserRepository(Map(userId -> oldUser))
    val service = new LiveNoteService[IO](fakeRepo)

    val updatedNote = "updatedNote"

    for {
      updatedUser <- service.updateSecretNote(userId, updatedNote)
    } yield assertEquals(updatedUser, oldUser.copy(secretNote = updatedNote))
  }

  test("updateSecretNote raises UserNotFoundException if user is missing") {
    val fakeRepo = new FakeUserRepository(Map())
    val service = new LiveNoteService[IO](fakeRepo)

    val updatedNote = "updatedNote"

    service.updateSecretNote(UUID.randomUUID(), updatedNote).intercept[UserNotFoundException]
  }

  test("getSecretNote returns note if user exists") {
    val userId = UUID.randomUUID()
    val testUser = User(userId, "user", "passwordHash", "note")

    val fakeRepo = new FakeUserRepository(Map(userId -> testUser))
    val service = new LiveNoteService[IO](fakeRepo)

    for {
      note <- service.getSecretNote(userId)
    } yield assertEquals(note, testUser.secretNote)
  }

  test("getSecretNote returns blank note if user exists, but doesn't have one") {
    val userId = UUID.fromString(emptyNoteUUID)
    val testUser = User(userId, "user", "passwordHash", "mockUnused")

    val fakeRepo = new FakeUserRepository(Map(userId -> testUser))
    val service = new LiveNoteService[IO](fakeRepo)

    for {
      note <- service.getSecretNote(userId)
    } yield assertEquals(note, "")
  }

  test("getSecretNote raises UserNotFoundException if user is missing") {
    val fakeRepo = new FakeUserRepository(Map.empty)
    val service = new LiveNoteService[IO](fakeRepo)

    service.getSecretNote(UUID.randomUUID()).intercept[UserNotFoundException]
  }
}
