package us.oyanglul.zhuyu
package jobs

import effects._
import effects.Logger._
import cats.instances.string._
import us.oyanglul.zhuyu.models.{PrepareOrder}

trait OnPrepareOrder {
  implicit val onPrepareOrder =
    new Job[PrepareOrder, effects.HasSQS] {
      private val logger = org.log4s.getLogger

      def distribute(message: Envelop[PrepareOrder]) =
        for {
          _ <- logger.Info(s"FINISHED (=^･ｪ･^=))ﾉ彡☆ $message")
        } yield ()
    }
}
