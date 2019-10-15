package us.oyanglul.zhuyu
package effects

import cats.data.Kleisli
import cats.effect.IO
import org.http4s.client._

trait HasHttp4s {
  val http4sClient: Client[IO]
}

object Http4s {
  def apply[A](run: Client[IO] => IO[A]): Kleisli[IO, HasHttp4s, A] =
    Kleisli(run).local(_.http4sClient)
}
