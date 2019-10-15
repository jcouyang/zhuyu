package us.oyanglul.zhuyu
package effects

import cats.data.Kleisli
import cats.effect.IO
import doobie.free.connection.ConnectionIO
import doobie._
import doobie.implicits._

trait HasDoobie {
  val doobieTx: Transactor[IO]
}

object Doobie {
  def apply[A](cio: ConnectionIO[A]): Kleisli[IO, HasDoobie, A] = Kleisli {
    has =>
      cio.transact(has.doobieTx)
  }
}
