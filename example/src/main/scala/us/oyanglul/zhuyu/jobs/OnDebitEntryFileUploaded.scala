package us.oyanglul.zhuyu
package jobs

import cats.syntax.functor._
import io.circe.generic.auto._
import us.oyanglul.zhuyu.models.{
  BillingServiceNotified,
  DebitEntryFileUploaded,
  Event
}

trait OnDebitEntryFileUploaded {
  implicit val onDebitEntryFileUploaded =
    new Job[DebitEntryFileUploaded, effects.HasSQS] {
      def distribute(message: DebitEntryFileUploaded) = {
        spread[Event](BillingServiceNotified(123)).void
      }
    }
}
