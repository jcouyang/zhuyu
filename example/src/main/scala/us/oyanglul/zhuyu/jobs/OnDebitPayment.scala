package us.oyanglul.zhuyu
package jobs
import io.circe.generic.auto._
import us.oyanglul.zhuyu.effects._
import us.oyanglul.zhuyu.models.{NotifyPaymentResult, Event, DebitPayment}

trait OnDebitPayment {
  implicit val onDebitPayment =
    new Job[DebitPayment, effects.HasSQS with effects.HasS3] {
      def distribute(message: Envelop[DebitPayment]) =
        for {
          resp <- S3(_.putObject("example-bucket", "filename", "content"))
          _ <- message.spread[Event](NotifyPaymentResult(resp.getETag))
        } yield ()
    }
}
