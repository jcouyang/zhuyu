package us.oyanglul.zhuyu
package jobs

import effects._
import io.circe.generic.auto._
import us.oyanglul.zhuyu.models.{Event, DebitPayment, InitPayment}
import org.http4s._
import org.http4s.dsl.io._
import org.http4s.client.dsl.io._

trait OnInitPayment {
  implicit val onInitPayment =
    new Job[InitPayment, HasSQS with HasSQSResponder with HasHttp4s] {
      override def distribute(message: Envelop[InitPayment]) =
        for {
          status <- effects.Http4s(_.status(GET(uri"https://blog.oyanglul.us")))
          _ <- message.spread[Event](DebitPayment(status.code))
          _ <- message.respond(status.code)
        } yield ()
    }
}
