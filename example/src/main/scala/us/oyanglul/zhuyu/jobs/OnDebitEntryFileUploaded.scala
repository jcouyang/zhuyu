package us.oyanglul.zhuyu
package jobs

import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import us.oyanglul.zhuyu.models.{
  BillingServiceNotified,
  DebitEntryFileUploaded,
  Event
}

trait OnDebitEntryFileUploaded {
  implicit val onDebitEntryFileUploaded =
    new Job[DebitEntryFileUploaded, effects.HasSQS with effects.HasHttp4s] {
      def distribute(message: effects.Envelop[DebitEntryFileUploaded]) =
        for {
          status <- effects.Http4s(
            _.status(GET(uri"https://blog.oyanglul.us/not-exist")))
          _ <- spread[Event](BillingServiceNotified(status.code))
        } yield ()
    }
}
