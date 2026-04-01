package com.github.ricemonger.secretnote.repository

import cats.effect.{IO, Resource}
import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials}
import com.github.ricemonger.secretnote.repository.UserRepository.LiveUserRepository
import com.dimafeng.testcontainers.PostgreSQLContainer
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import munit.CatsEffectSuite
import org.flywaydb.core.Flyway
import org.testcontainers.utility.DockerImageName
import doobie.implicits.*

import java.util.TimeZone

class UserRepositoryIT extends CatsEffectSuite {

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  val postgresResource: Resource[IO, PostgreSQLContainer] = Resource.make(IO.blocking {
    val container = PostgreSQLContainer.Def(
      dockerImageName = DockerImageName.parse("postgres:18.3")
    ).start()
    Flyway.configure()
      .dataSource(container.jdbcUrl, container.username, container.password)
      .load()
      .migrate()
    container
  })(c => IO.blocking(c.stop()))

  val transactorResource: Resource[IO, HikariTransactor[IO]] = for {
    container <- postgresResource
    ce        <- ExecutionContexts.fixedThreadPool[IO](1)
    xa        <- HikariTransactor.newHikariTransactor[IO](
      "org.postgresql.Driver",
      container.jdbcUrl,
      container.username,
      container.password,
      ce
    )
  } yield xa

  val transactorFixture = ResourceSuiteLocalFixture("transactor", transactorResource)

  def dbTest(name: String)(testBody: HikariTransactor[IO] => IO[Unit]): Unit = {
    test(name) {
      val xa = transactorFixture()

      for {
        _ <- sql"TRUNCATE TABLE users CASCADE".update.run.transact(xa)
        _ <- testBody(xa)
      } yield ()
    }
  }

  override def munitFixtures = List(transactorFixture)

  dbTest("insertCredentials should insert new user and return it") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val creds = UserCredentials("user1", "hashedpassword")

    for {
      insertedOpt <- repo.insertCredentials(creds)
      user <- IO.fromOption(insertedOpt)(new Exception("User was not inserted"))
    } yield {
      assertEquals(user.username, creds.username)
      assertEquals(user.passwordHash, creds.passwordHash)
      assertEquals(user.secretNote, "")
    }
  }

  dbTest("insertCredentials should return None on conflict") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val creds = UserCredentials("conflict", "hash1")

    for {
      firstInsert <- repo.insertCredentials(creds)

      secondInsert <- repo.insertCredentials(creds.copy(passwordHash = "hash2"))
    } yield {
      assert(firstInsert.isDefined)
      assertEquals(secondInsert, None)
    }
  }

  dbTest("selectByUsername should return user if exist") { xa =>
    val repo = new LiveUserRepository[IO](xa)

    val username = "username"

    val creds = UserCredentials(username, "hash")

    for {
      _ <- repo.insertCredentials(creds)
      foundUserOpt <- repo.selectByUsername(username)
    } yield {
      assert(foundUserOpt.isDefined)
      assertEquals(foundUserOpt.get.username, username)
    }
  }

  dbTest("selectByUsername should return None if user doesn't exist") { xa =>
    val repo = new LiveUserRepository[IO](xa)

    for {
      foundUserOpt <- repo.selectByUsername("new_user")
    } yield {
      assertEquals(foundUserOpt, None)
    }
  }

  dbTest("updateNoteById should update secret note and return user") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val creds = UserCredentials("user", "hash")

    val newNote = "newNote"

    for {
      insertedUser <- repo.insertCredentials(creds).map(_.get)
      updatedUserOpt <- repo.updateNoteById(insertedUser.id, newNote)
    } yield {
      assert(updatedUserOpt.isDefined)
      assertEquals(updatedUserOpt.get.secretNote, newNote)
    }
  }

  dbTest("updateNoteById should return None if user ID doesn't exist") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val randomId = java.util.UUID.randomUUID()

    for {
      updatedUserOpt <- repo.updateNoteById(randomId, "unused")
    } yield {
      assertEquals(updatedUserOpt, None)
    }
  }

  dbTest("selectNoteById should return note if user exists") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val creds = UserCredentials("user", "hash")

    val note = "note"

    for {
      insertedUser <- repo.insertCredentials(creds).map(_.get)

      initialNoteOpt <- repo.selectNoteById(insertedUser.id)

      _ <- repo.updateNoteById(insertedUser.id, "note")

      updatedNoteOpt <- repo.selectNoteById(insertedUser.id)
    } yield {
      assertEquals(initialNoteOpt, Some(Some("")))
      assertEquals(updatedNoteOpt, Some(Some("note")))
    }
  }

  dbTest("selectNoteById should return None if user ID doesn't exist") { xa =>
    val repo = new LiveUserRepository[IO](xa)
    val randomId = java.util.UUID.randomUUID()

    for {
      noteOpt <- repo.selectNoteById(randomId)
    } yield {
      assertEquals(noteOpt, None)
    }
  }
}