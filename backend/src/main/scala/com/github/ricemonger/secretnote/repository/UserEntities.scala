package com.github.ricemonger.secretnote.repository

import com.github.ricemonger.secretnote.domain.user.{User, UserCredentials, UserInfo}

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

private[repository] case class UserInfoProjection(
                                                   username: String,
                                                   password_hash: String,
                                                   secret_note: String
                                                 )

private[repository] object UserInfoProjection {
  def fromDomain(userInfo: UserInfo): UserInfoProjection = {
    UserInfoProjection(
      username = userInfo.username,
      password_hash = userInfo.passwordHash,
      secret_note = userInfo.secretNote
    )
  }
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

