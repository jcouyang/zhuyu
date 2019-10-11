package us.oyanglul.zhuyu

import cats.data.Kleisli
import effects._
import Logger._
import cats.effect.IO
import io.circe.parser._
import io.circe.Decoder
import cats.syntax.traverse._
import cats.instances.list._
import cats.instances.string._
import cats.syntax.applicativeError._
import com.amazonaws.services.sqs.model._
import jobs.Job

object Worker {
  val logger = org.log4s.getLogger
  def work[A, Has <: HasSQS](
      implicit ev: Decoder[A],
      job: Job[A, Has]): Kleisli[IO, Has, List[Either[Throwable, Unit]]] =
    for {
      messages <- SQS.pollMessage
      _ <- logger.Debug(s"polled ${messages.length} messages")
      results <- messages.traverse(message => eachWork(message).attempt)
      _ <- logger.Debug(s"processed ${messages.length} messages")
    } yield results

  private def eachWork[A, Has <: HasSQS](message: Message)(implicit
                                                           ev: Decoder[A],
                                                           job: Job[A, Has]) =
    for {
      event <- Kleisli.liftF(IO.fromEither(decode[A](message.getBody)))
      _ <- job.distribute(event)
      _ <- logger.Debug(s"working on message ${message.getMessageId}: $event")
      _ <- SQS.deleteMessage(message.getReceiptHandle)
      _ <- logger.Debug(s"deleted message ${message.getMessageId}")
    } yield ()
}
