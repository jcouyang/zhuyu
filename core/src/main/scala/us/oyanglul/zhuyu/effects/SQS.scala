package us.oyanglul.zhuyu
package effects

import com.amazonaws.services.sqs.model._
import cats.data.Kleisli
import cats.effect.IO

import scala.collection.JavaConverters._
import io.circe.syntax._
import com.amazonaws.services.sqs.AmazonSQS
import io.circe.Encoder

case class SQSConfig(
    sqsQueueUrl: String,
    longPollSeconds: Int
)

trait HasSQS {
  val sqsConfig: SQSConfig
  val sqsClient: AmazonSQS
}
case class EnvelopCover(id: String, hash: String)
case class Envelop[+A](cover: EnvelopCover, content: A)
object SQS {
  def pollMessage: Kleisli[IO, HasSQS, List[Message]] =
    Kleisli { has =>
      val config = has.sqsConfig
      val pollRequest = new ReceiveMessageRequest(config.sqsQueueUrl)
        .withWaitTimeSeconds(config.longPollSeconds)
      IO.delay(
        has.sqsClient.receiveMessage(pollRequest).getMessages.asScala.toList)
    }

  def sendMessageFrom[Event, FromEvent <: Event]
    : SafeSendMessage[Event, FromEvent] =
    new SafeSendMessage[Event, FromEvent]

  def deleteMessage(handler: String): Kleisli[IO, HasSQS, DeleteMessageResult] =
    Kleisli { has =>
      IO.delay(
        has.sqsClient.deleteMessage(
          new DeleteMessageRequest(has.sqsConfig.sqsQueueUrl, handler)))
    }
}
final class SafeSendMessage[Event, FromEvent <: Event] {
  def apply[ToEvent <: Event](evt: ToEvent)(
      implicit
      ev: CycleBreaker[Event, FromEvent, ToEvent],
      ed: Encoder[Event]
  ): Kleisli[IO, HasSQS, SendMessageResult] =
    Kleisli { has =>
      IO.delay(
        has.sqsClient.sendMessage(
          new SendMessageRequest(has.sqsConfig.sqsQueueUrl,
                                 (evt: Event).asJson.noSpaces)))
    }
}
