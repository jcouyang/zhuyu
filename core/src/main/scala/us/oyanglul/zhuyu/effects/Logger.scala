package us.oyanglul.zhuyu.effects

import cats.data.Kleisli
import cats.effect.IO
import cats.Show
import cats.syntax.show._
import io.chrisdavenport.log4cats

trait HasLogger {
  val logger: log4cats.Logger[IO]
}

object Logger {
  def apply[A](run: log4cats.Logger[IO] => IO[Unit]): Kleisli[IO, HasLogger, Unit] =
    Kleisli(run).local(_.logger)
}
