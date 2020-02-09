package us.oyanglul.zhuyu
package jobs

import io.circe.generic.auto._
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._
import us.oyanglul.zhuyu.models.{PrepareOrder, NotifyPaymentResult, Event}

trait OnNotifyPaymentResult {
  implicit val onNotifyPaymentResult =
    new Job[NotifyPaymentResult, effects.HasSQS with effects.HasHttp4s] {
      def distribute(message: effects.Envelop[NotifyPaymentResult]) =
        for {
          status <- effects.Http4s(
            _.status(GET(uri"https://blog.oyanglul.us/not-exist")))
          _ <- message.spread[Event](PrepareOrder(status.code))
        } yield ()
    }
}
