package us.oyanglul.zhuyu
package jobs

import cats.effect.IO
import us.oyanglul.zhuyu.models.DebitEntryProcessed

trait OnDebitEntryProcessed {
  implicit val onDebitEntryProcessed =
    new Job[DebitEntryProcessed, effects.HasSQS] {
      def distribute(message: DebitEntryProcessed) = {
        liftF(IO(println(message)))
      }
    }
}
