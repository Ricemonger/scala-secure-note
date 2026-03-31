package com.github.ricemonger.secretnote.repository

import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials}

import java.util.UUID

private[repository] case class UserEntity(
                                           id: UUID,
                                           username: String,
                                           password_hash: String,
                                           secret_note: String
                                         ) {
  def toDomain: User = User(
    id = this.id,
    username = this.username,
    passwordHash = this.password_hash,
    secretNote = this.secret_note
  )
}

private[repository] case class UserCredentialsProjection(
                                                          username: String,
                                                          password_hash: String,
                                                        ) {
  def toDomain: UserCredentials = UserCredentials(
    username = this.username,
    passwordHash = this.password_hash,
  )
}

private[repository] object UserCredentialsProjection {
  def fromDomain(userCredentials: UserCredentials): UserCredentialsProjection = {
    UserCredentialsProjection(
      username = userCredentials.username,
      password_hash = userCredentials.passwordHash
    )
  }
}

