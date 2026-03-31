package com.github.ricemonger.secretnote.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class JwtConfig(secret: String, expiration: Int)

object JwtConfig {
  given ConfigReader[JwtConfig] = deriveReader[JwtConfig]
}
