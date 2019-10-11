package us.oyanglul.zhuyu
package jobs
import io.circe.generic.auto._
import cats.syntax.functor._
import us.oyanglul.zhuyu.models.{DebitEntryFileUploaded, Event, PaymentDebited}

trait OnPaymentDebited {
  implicit val onPaymentDebited =
    new Job[PaymentDebited, effects.HasSQS] {
      def distribute(message: PaymentDebited) = {
        spread[Event](DebitEntryFileUploaded(123)).void
      }
    }
}
