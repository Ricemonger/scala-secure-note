package com.github.ricemonger.secretnote.exception

case class UserNotFound(username: String) extends UserDomainException(s"User with username: $username not found")
