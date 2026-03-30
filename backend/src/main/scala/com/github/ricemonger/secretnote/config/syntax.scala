package com.github.ricemonger.secretnote.config

import cats.MonadThrow
import cats.implicits.*
import pureconfig.error.ConfigReaderException
import pureconfig.{ConfigReader, ConfigSource}

import scala.reflect.ClassTag

object syntax {
  extension (source: ConfigSource) {
    def loadF[F[_], A](using configReader: ConfigReader[A], f: MonadThrow[F], tag: ClassTag[A]): F[A] = {
      f.pure(source.load[A]).flatMap {
        case Left(errors) => f.raiseError[A](ConfigReaderException(errors))
        case Right(value) => f.pure(value)
      }
    }
  }
}
