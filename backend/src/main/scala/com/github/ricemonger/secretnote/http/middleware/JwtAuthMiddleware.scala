package com.github.ricemonger.secretnote.http.middleware

import cats.data.{Kleisli, OptionT}
import cats.effect.Sync
import cats.syntax.all.*
import com.github.ricemonger.secretnote.config.JwtConfig
import com.github.ricemonger.secretnote.domain.user.UserJwtPayload
import org.http4s.*
import org.http4s.headers.Authorization
import org.http4s.server.AuthMiddleware
import org.typelevel.ci.CIString
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtOptions}

import java.util.UUID

object JwtAuthMiddleware {

  def apply[F[_] : Sync](config: JwtConfig): AuthMiddleware[F, UserJwtPayload] = {

    val authUser = Kleisli { (req: Request[F]) =>
      val payloadOpt = for {
        authHeader <- req.headers.get[Authorization]

        token <- authHeader.credentials match {
          case Credentials.Token(scheme, tokenValue) if scheme == CIString("Bearer") => Some(tokenValue)
          case _ => None
        }

        claim <- JwtCirce.decode(token, config.secret, Seq(JwtAlgorithm.HS256), JwtOptions.DEFAULT).toOption

        json <- io.circe.parser.parse(claim.content).toOption
        idStr <- json.hcursor.get[String]("id").toOption

        id <- Either.catchNonFatal(UUID.fromString(idStr)).toOption
      } yield UserJwtPayload(id)

      Sync[F].pure(payloadOpt.toRight("Invalid or missing JWT token"))
    }

    val onFailure: AuthedRoutes[String, F] = Kleisli { req =>
      OptionT.liftF(
        Sync[F].pure(Response[F](Status.Unauthorized).withEntity(req.context))
      )
    }

    AuthMiddleware(authUser, onFailure)
  }
}