package us.oyanglul.zhuyu
package impls

import cats.effect.{Blocker, IO}

import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor

trait HasDoobieImpl extends effects.HasDoobie {
  private implicit val cs = IO.contextShift(ExecutionContexts.synchronous)
  val doobieTx = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver", // driver classname
    "jdbc:postgresql://db:5432/zhuyu", // connect URL (driver-specific)
    "postgres", // user
    "", // password
    Blocker
      .liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )
}
