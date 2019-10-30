package us.oyanglul.zhuyu
package jobs

import us.oyanglul.zhuyu.models.DebitEntryProcessed
import effects.Logger._
import cats.instances.string._

trait OnDebitEntryProcessed {
  val logger = org.log4s.getLogger
  implicit val onDebitEntryProcessed =
    new Job[DebitEntryProcessed, effects.HasSQS] {
      def distribute(message: effects.Envelop[DebitEntryProcessed]) =
        logger.Info(s"FINISHED (=^･ｪ･^=))ﾉ彡☆ $message")
    }
}
