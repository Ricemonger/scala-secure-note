package com.github.ricemonger.secretnote.exception

case class UserAlreadyExists(username: String) extends UserDomainException(s"User with username: $username already exists")


