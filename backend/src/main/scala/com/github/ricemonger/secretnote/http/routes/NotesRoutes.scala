package com.github.ricemonger.secretnote.http.routes

import cats.effect.Concurrent
import cats.syntax.all.*
import com.github.ricemonger.secretnote.domain.user.{User, UserJwtPayload}
import com.github.ricemonger.secretnote.service.NoteService.NoteService
import org.http4s.*
import org.http4s.dsl.Http4sDsl
import org.typelevel.log4cats.Logger

class NotesRoutes[F[_] : Concurrent] private(noteService: NoteService[F]) extends Http4sDsl[F] {

  val authedRoutes: AuthedRoutes[UserJwtPayload, F] = AuthedRoutes.of {

    case GET -> Root as jwtPayload =>
      for {
        note <- noteService.getSecretNote(jwtPayload.id)
        resp <- Ok(note)
      } yield resp

    case req@PUT -> Root as jwtPayload =>
      for {
        noteText <- req.req.as[String]
        _ <- noteService.updateSecretNote(jwtPayload.id, noteText)
        resp <- Ok("Note updated successfully")
      } yield resp
  }
}

object NotesRoutes {
  def apply[F[_] : {Concurrent, Logger}](noteService: NoteService[F]) = new NotesRoutes[F](noteService)
}