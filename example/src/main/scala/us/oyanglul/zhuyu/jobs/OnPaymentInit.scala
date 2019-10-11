package us.oyanglul.zhuyu
package jobs
import io.circe.generic.auto._
import cats.syntax.functor._
import us.oyanglul.zhuyu.models.{Event, PaymentDebited, PaymentInited}

trait OnPaymentInited {
  implicit val onPaymentInited =
    new Job[PaymentInited, effects.HasSQS] {
      def distribute(message: PaymentInited) = {
        spread[Event](PaymentDebited(123)).void
      }
    }
}
