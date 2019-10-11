package us.oyanglul.zhuyu
package jobs

import cats.syntax.functor._
import io.circe.generic.auto._
import us.oyanglul.zhuyu.models.{
  BillingServiceNotified,
  DebitEntryProcessed,
  Event
}

trait OnBillingServiceNotified {
  implicit val onBillingServiceNotified =
    new Job[BillingServiceNotified, effects.HasSQS] {
      def distribute(message: BillingServiceNotified) = {
        spread[Event](DebitEntryProcessed(123)).void
      }
    }
}
