package com.github.ricemonger.secretnote.domain

import java.util.UUID

object user {

  case class User(
                   id: UUID,
                   username: String,
                   passwordHash: String,
                   secretNote: String
                 )
  
  case class UserCredentials(
                              username: String,
                              passwordHash: String,
                            )
}
