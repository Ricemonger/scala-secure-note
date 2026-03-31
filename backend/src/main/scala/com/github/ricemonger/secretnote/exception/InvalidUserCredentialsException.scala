package com.github.ricemonger.secretnote.exception

case class InvalidUserCredentialsException() extends UserDomainException(s"Invalid Credentials")
