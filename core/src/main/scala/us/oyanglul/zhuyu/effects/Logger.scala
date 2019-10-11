package us.oyanglul.zhuyu.effects

import cats.data.Kleisli
import cats.effect.IO
import cats.Show
import cats.syntax.show._
import org.log4s

object Logger {
  implicit class Log4sSyntaxWrapper(logger: log4s.Logger) {
    def Info[F: Show](content: F): Kleisli[IO, Any, Unit] = Kleisli { _ =>
      IO.delay(logger.info(content.show))
    }

    def Error[F: Show](content: F): Kleisli[IO, Any, Unit] = Kleisli { _ =>
      IO.delay(logger.error(content.show))
    }

    def Debug[F: Show](content: F): Kleisli[IO, Any, Unit] = Kleisli { _ =>
      IO.delay(logger.debug(content.show))
    }
  }
}
