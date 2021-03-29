package us.oyanglul.zhuyu
package effects

import com.amazonaws.services.sqs.model._
import cats.data.Kleisli
import cats.effect.IO

import scala.collection.JavaConverters._
import io.circe.syntax._
import io.circe.Encoder
import com.amazonaws.services.sqs._
import java.util.concurrent.TimeUnit

import scala.util.Try

case class SQSConfig(
    sqsQueueUrl: String,
    longPollSeconds: Int
)

trait HasSQSConfig {
  val sqsConfig: SQSConfig
}
trait HasSQS extends HasSQSConfig {
  val sqsClient: AmazonSQS
}

trait HasSQSResponder extends HasSQSConfig {
  val sqsResponder: AmazonSQSResponder
}
trait HasSQSRequester extends HasSQSConfig {
  val sqsRequester: AmazonSQSRequester
}
case class EnvelopCover(
    id: String,
    hash: String,
    attributes: Map[String, String],
    messageAttributes: Map[String, MessageAttributeValue]
) {
  def responseQueueUrl =
    messageAttributes.get(SQS.responseQueueUrlKey).map(_.getStringValue)
}
case class Envelop[A](cover: EnvelopCover, content: A) {
  def spread[C >: A] =
    new SafeSendMessage[C, A](Some(cover))
  def respond[B: Encoder](ev: B) = SQS.respond[B](cover, ev)
}
object SQS {
  val responseQueueUrlKey = "ResponseQueueUrl"
  def pollMessage: Kleisli[IO, HasSQS, List[Message]] =
    Kleisli { has =>
      val config = has.sqsConfig
      val pollRequest = new ReceiveMessageRequest(config.sqsQueueUrl)
        .withWaitTimeSeconds(config.longPollSeconds)
        .withMessageAttributeNames(responseQueueUrlKey)
      IO.delay {
        has.sqsClient.receiveMessage(pollRequest).getMessages.asScala.toList
      }
    }

  def deleteMessage(handler: String): Kleisli[IO, HasSQS, DeleteMessageResult] =
    Kleisli { has =>
      IO.delay(
        has.sqsClient.deleteMessage(
          new DeleteMessageRequest(has.sqsConfig.sqsQueueUrl, handler)))
    }
  def request[A: Encoder](
      request: A,
      timeout: Int = 30): Kleisli[IO, HasSQSRequester, Message] =
    Kleisli { has =>
      val message = new SendMessageRequest()
        .withMessageBody(request.asJson.noSpaces)
        .withQueueUrl(has.sqsConfig.sqsQueueUrl)

      IO.async_ { cb =>
        Try {
          has.sqsRequester.sendMessageAndGetResponse(message,
                                                     timeout,
                                                     TimeUnit.SECONDS)
        }.fold(a => cb(Left(a)), b => cb(Right(b)))
      }
    }
  def respond[A: Encoder](cover: EnvelopCover,
                          response: A): Kleisli[IO, HasSQSResponder, Unit] =
    Kleisli { has =>
      IO.delay(
        has.sqsResponder.sendResponseMessage(
          new MessageContent("", cover.messageAttributes.asJava),
          new MessageContent(response.asJson.noSpaces))
      )
    }
  def respond[A: Encoder](url: String,
                          response: A): Kleisli[IO, HasSQSResponder, Unit] =
    Kleisli { has =>
      IO.delay(
        has.sqsResponder.sendResponseMessage(
          new MessageContent(
            "",
            Map(
              responseQueueUrlKey -> new MessageAttributeValue()
                .withStringValue(url)).asJava),
          new MessageContent(response.asJson.noSpaces))
      )
    }
}
final class SafeSendMessage[Event, FromEvent <: Event](
    cover: Option[EnvelopCover]) {
  def apply[ToEvent <: Event](evt: ToEvent)(
      implicit
      ev: CycleBreaker[Event, FromEvent, ToEvent],
      ed: Encoder[Event]
  ): Kleisli[IO, HasSQS, SendMessageResult] =
    Kleisli { has =>
      IO.delay {
        val message = new SendMessageRequest(has.sqsConfig.sqsQueueUrl,
                                             (evt: Event).asJson.noSpaces)
        has.sqsClient.sendMessage(cover.fold(message)(c =>
          message.withMessageAttributes(c.messageAttributes.asJava)))
      }
    }
}
