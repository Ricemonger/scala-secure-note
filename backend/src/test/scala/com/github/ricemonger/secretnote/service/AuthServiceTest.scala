package com.github.ricemonger.secretnote.service

import cats.effect.IO
import com.github.ricemonger.secretnote.config.JwtConfig
import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials}
import com.github.ricemonger.secretnote.exception.{InvalidUserCredentialsException, UserAlreadyExistsException}
import com.github.ricemonger.secretnote.repository.UserRepository.UserRepository
import com.github.ricemonger.secretnote.service.AuthService.LiveAuthService
import munit.CatsEffectSuite
import tsec.passwordhashers.jca.BCrypt
import tsec.common.Verified
import tsec.passwordhashers.PasswordHash

import java.util.UUID

class AuthServiceTest extends CatsEffectSuite {

  val jwtConfig = JwtConfig(secret = "super-secret-test-key-1234567890", expiration = 3600)

  class FakeUserRepository(initialData: Map[String, User] = Map.empty) extends UserRepository[IO] {
    var data = initialData

    override def insertCredentials(uc: UserCredentials): IO[Option[User]] = IO.pure {
      if (data.contains(uc.username)) {
        None
      } else {
        val newUser = User(UUID.randomUUID(), uc.username, uc.passwordHash, "")
        data = data + (uc.username -> newUser)
        Some(newUser)
      }
    }

    override def selectByUsername(username: String): IO[Option[User]] = IO.pure {
      data.get(username)
    }

    override def updateNoteById(id: UUID, secretNote: String): IO[Option[User]] = IO.pure(None)
    override def selectNoteById(id: UUID): IO[Option[Option[String]]] = IO.pure(None)
  }

  test("register successfully creates user, hashes password, and returns a JWT") {
    val fakeRepo = new FakeUserRepository()
    val service = new LiveAuthService[IO](fakeRepo, jwtConfig)

    for {
      jwt <- service.register("newUser", "password")

      storedUser = fakeRepo.data("newUser")

      isVerified <- BCrypt.checkpw[IO](
        "password",
        PasswordHash[BCrypt](storedUser.passwordHash)
      )
    } yield {
      assert(jwt.nonEmpty)
      assert(fakeRepo.data.contains("newUser"))

      assertNotEquals(storedUser.passwordHash, "password")

      assertEquals(isVerified, Verified)
    }
  }

  test("register raises UserAlreadyExistsException if username is taken") {
    val existingUser = User(UUID.randomUUID(), "takenUser", "hash", "")
    val fakeRepo = new FakeUserRepository(Map("takenUser" -> existingUser))
    val service = new LiveAuthService[IO](fakeRepo, jwtConfig)

    service.register("takenUser", "newPassword").intercept[UserAlreadyExistsException]
  }

  test("login returns JWT when credentials are valid") {
    for {
      hash <- BCrypt.hashpw[IO]("password")

      testUser = User(UUID.randomUUID(), "testUser", hash, "")
      fakeRepo = new FakeUserRepository(Map("testUser" -> testUser))
      service = new LiveAuthService[IO](fakeRepo, jwtConfig)

      jwt <- service.login("testUser", "password")
    } yield assert(jwt.nonEmpty)
  }

  test("login raises InvalidUserCredentialsException when user doesn't exist") {
    val fakeRepo = new FakeUserRepository()
    val service = new LiveAuthService[IO](fakeRepo, jwtConfig)

    service.login("nonExistentUser", "password").intercept[InvalidUserCredentialsException]
  }

  test("login raises InvalidUserCredentialsException when password doesn't match") {
    for {
      hash <- BCrypt.hashpw[IO]("correctPassword")

      testUser = User(UUID.randomUUID(), "testUser", hash, "")
      fakeRepo = new FakeUserRepository(Map("testUser" -> testUser))
      service = new LiveAuthService[IO](fakeRepo, jwtConfig)

      _ <- service.login("testUser", "wrongPassword").intercept[InvalidUserCredentialsException]
    } yield ()
  }
}