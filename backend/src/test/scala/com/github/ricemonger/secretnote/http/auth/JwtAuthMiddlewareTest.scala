package com.github.ricemonger.secretnote.http.auth

import cats.effect.IO
import com.github.ricemonger.secretnote.config.JwtConfig
import com.github.ricemonger.secretnote.domain.user.UserJwtPayload
import munit.CatsEffectSuite
import org.http4s.*
import org.http4s.dsl.io.*
import org.http4s.headers.Authorization
import org.http4s.implicits.uri
import org.typelevel.ci.CIString
import pdi.jwt.{JwtAlgorithm, JwtCirce, JwtClaim}

import java.time.Instant
import java.util.UUID

class JwtAuthMiddlewareTest extends CatsEffectSuite {

  val jwtConfig = JwtConfig("test-secret-key-1234567890", 3600)
  val jwtAuthMiddleware = JwtAuthMiddleware[IO](jwtConfig)

  val authedRoutes = AuthedRoutes.of[UserJwtPayload, IO] {
    case GET -> Root as payload => Ok(s"Ok ${payload.id}")
  }

  val securedRoutes = jwtAuthMiddleware(authedRoutes)

  def generateToken(id: UUID, expired: Boolean = false): String = {
    val expiration = if (expired) Instant.now().minusSeconds(3600) else Instant.now().plusSeconds(3600)
    val claim = JwtClaim(
      content = s"""{"id": "$id"}""",
      expiration = Some(expiration.getEpochSecond)
    )
    JwtCirce.encode(claim, jwtConfig.secret, JwtAlgorithm.HS256)
  }

  test("returns 401 Unauthorized when Authorization header is missing") {
    val req = Request[IO](Method.GET, uri"/")

    for {
      resp <- securedRoutes.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Unauthorized)
  }

  test("returns 401 Unauthorized when JWT token is invalid") {
    val authHeader = Authorization(Credentials.Token(CIString("Bearer"), "invalid.token.string"))
    val req = Request[IO](Method.GET, uri"/").withHeaders(authHeader)

    for {
      resp <- securedRoutes.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Unauthorized)
  }

  test("returns 401 Unauthorized when JWT token is expired") {
    val expiredToken = generateToken(UUID.randomUUID(), expired = true)
    val authHeader = Authorization(Credentials.Token(CIString("Bearer"), expiredToken))
    val req = Request[IO](Method.GET, uri"/").withHeaders(authHeader)

    for {
      resp <- securedRoutes.orNotFound.run(req)
    } yield assertEquals(resp.status, Status.Unauthorized)
  }

  test("allows access and extracts payload when JWT token is valid") {
    val userId = UUID.randomUUID()
    val validToken = generateToken(userId)
    val authHeader = Authorization(Credentials.Token(CIString("Bearer"), validToken))
    val req = Request[IO](Method.GET, uri"/").withHeaders(authHeader)

    for {
      resp <- securedRoutes.orNotFound.run(req)
      body <- resp.as[String]
    } yield {
      assertEquals(resp.status, Status.Ok)
      assertEquals(body, s"Ok $userId")
    }
  }
}