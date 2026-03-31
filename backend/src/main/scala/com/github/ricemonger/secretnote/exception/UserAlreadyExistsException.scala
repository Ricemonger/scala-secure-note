package com.github.ricemonger.secretnote.exception

case class UserAlreadyExistsException(username: String) extends UserDomainException(s"User with username: $username already exists")


