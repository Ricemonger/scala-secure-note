package com.github.ricemonger.secretnote.exception

case class UserNotFoundException(username: String) extends UserDomainException(s"User with username: $username not found")
