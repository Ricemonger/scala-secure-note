package com.github.ricemonger.secretnote.config

import pureconfig.ConfigReader
import pureconfig.generic.semiauto.deriveReader

case class ConstraintConfig(
                             usernameRegex: String,
                             usernameMessage: String,
                             passwordRegex: String,
                             passwordMessage: String
                           )

object ConstraintConfig {
  given ConfigReader[ConstraintConfig] = deriveReader[ConstraintConfig]
}
