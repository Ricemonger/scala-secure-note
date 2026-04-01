package com.github.ricemonger.secretnote.http.routes

import cats.effect.IO
import com.github.ricemonger.secretnote.domain.user.{User, UserJwtPayload}
import com.github.ricemonger.secretnote.service.NoteService.NoteService
import io.circe.generic.auto.*
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.implicits.*
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.noop.NoOpLogger

import java.util.UUID

class NotesRoutesTest extends CatsEffectSuite {
  
  implicit val logger: Logger[IO] = NoOpLogger[IO]

  class FakeNoteService(
                         getNoteResult: IO[String] = IO.pure("note"),
                         updateNoteResult: IO[User] = IO.pure(User(UUID.randomUUID(), "testUser", "hash", "updated note"))
                       ) extends NoteService[IO] {
    override def updateSecretNote(id: UUID, note: String): IO[User] = updateNoteResult
    override def getSecretNote(id: UUID): IO[String] = getNoteResult
  }

  test("GET / returns 200 OK and the mapped NotePayload") {
    val expectedNote = "secret note"
    val service = new FakeNoteService(getNoteResult = IO.pure(expectedNote))
    val authedRoutes = NotesRoutes[IO](service).authedRoutes

    val payload = UserJwtPayload(UUID.randomUUID())
    val req = Request[IO](Method.GET, uri"/")
    val authedReq = AuthedRequest(payload, req)

    for {
      respOpt <- authedRoutes.run(authedReq).value
      resp = respOpt.get
      body <- resp.as[NotePayload]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body.secretNote, expectedNote)
    }
  }

  test("PUT / updates note and returns 200 OK success message") {
    val service = new FakeNoteService()
    val authedRoutes = NotesRoutes[IO](service).authedRoutes

    val payload = UserJwtPayload(UUID.randomUUID())
    val req = Request[IO](Method.PUT, uri"/").withEntity(NotePayload("new note"))
    val authedReq = AuthedRequest(payload, req)

    for {
      respOpt <- authedRoutes.run(authedReq).value
      resp = respOpt.get
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assert(body.contains("Note updated successfully"))
    }
  }
}