package com.github.ricemonger.secretnote.http.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.github.ricemonger.secretnote.domain.user.{User, UserJwtPayload}
import com.github.ricemonger.secretnote.service.NoteService.NoteService
import io.circe.generic.auto.*
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

case class NotePayload(secretNote: String)

class NotesRoutes[F[_] : Concurrent] private(noteService: NoteService[F]) extends Http4sDsl[F] {

  val authedRoutes: AuthedRoutes[UserJwtPayload, F] = AuthedRoutes.of {

    case GET -> Root as jwtPayload =>
      for {
        note <- noteService.getSecretNote(jwtPayload.id)
        resp <- Ok(NotePayload(note))
      } yield resp

    case req@PUT -> Root as jwtPayload =>
      for {
        payload <- req.req.as[NotePayload]
        updatedUser <- noteService.updateSecretNote(jwtPayload.id, payload.secretNote)
        resp <- Ok(NotePayload(updatedUser.secretNote))
      } yield resp
  }
}

object NotesRoutes {
  def apply[F[_] : {Concurrent, Logger}](noteService: NoteService[F]) = new NotesRoutes[F](noteService)
}