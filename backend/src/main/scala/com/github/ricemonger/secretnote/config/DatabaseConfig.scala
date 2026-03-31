package com.github.ricemonger.secretnote.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class DatabaseConfig(driver: String, url: String, user: String, password: String)

object DatabaseConfig {
  given ConfigReader[DatabaseConfig] = deriveReader[DatabaseConfig]
}