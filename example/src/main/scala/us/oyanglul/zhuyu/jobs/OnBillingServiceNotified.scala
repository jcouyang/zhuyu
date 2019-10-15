package us.oyanglul.zhuyu
package jobs

import doobie.implicits._
import io.circe.generic.auto._
import us.oyanglul.zhuyu.models.{
  BillingServiceNotified,
  DebitEntryProcessed,
  Event
}

trait OnBillingServiceNotified {
  implicit val onBillingServiceNotified =
    new Job[BillingServiceNotified, effects.HasSQS with effects.HasDoobie] {
      def distribute(message: BillingServiceNotified) =
        for {
          id <- effects.Doobie(sql"select 250".query[Int].unique)
          _ <- spread[Event](DebitEntryProcessed(id))
        } yield ()
    }
}
