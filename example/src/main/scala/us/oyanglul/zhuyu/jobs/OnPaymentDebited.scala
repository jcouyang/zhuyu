package us.oyanglul.zhuyu
package jobs
import io.circe.generic.auto._
import us.oyanglul.zhuyu.effects.S3
import us.oyanglul.zhuyu.models.{DebitEntryFileUploaded, Event, PaymentDebited}

trait OnPaymentDebited {
  implicit val onPaymentDebited =
    new Job[PaymentDebited, effects.HasSQS with effects.HasS3] {
      def distribute(message: PaymentDebited) =
        for {
          resp <- S3(_.putObject("example-bucket", "filename", "content"))
          _ <- spread[Event](DebitEntryFileUploaded(resp.getETag))
        } yield ()
    }
}
