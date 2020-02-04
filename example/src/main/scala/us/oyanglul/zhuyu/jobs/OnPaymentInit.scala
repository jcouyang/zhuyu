package us.oyanglul.zhuyu
package jobs
import io.circe.generic.auto._
import us.oyanglul.zhuyu.models.{Event, PaymentDebited, PaymentInited}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._

trait OnPaymentInited {
  implicit val onPaymentInited =
    new Job[
      PaymentInited,
      effects.HasSQS with effects.HasSQSResponder with effects.HasHttp4s] {
      override def distribute(message: effects.Envelop[PaymentInited]) =
        for {
          status <- effects.Http4s(_.status(GET(uri"https://blog.oyanglul.us")))
          _ <- spread[Event](PaymentDebited(status.code))
          _ <- effects.SQS.respond(message.cover, status.code)
        } yield ()
    }
}
