package us.oyanglul.zhuyu

import models._
import io.circe.generic.auto._
import effects._
import cats.effect._
import cats.implicits._
import fs2._

object Main extends IOApp {
  val logger = org.log4s.getLogger

  object impl
      extends impls.HasSQSImpl
      with impls.HasHttp4sImpl
      with impls.HasS3Impl
      with impls.HasDoobieImpl

  def run(args: List[String]): IO[ExitCode] = {
    Stream
      .repeatEval {
        Worker
          .work[Event, HasSQS with HasHttp4s with HasS3 with HasDoobie]
          .run(impl)
          .map {
            _.separate match {
              case (errors, _) =>
                errors.map { e =>
                  logger.error(e.getMessage)
                }
            }
          }
      }
      .compile
      .drain
      .map(_ => ExitCode.Error)
  }
}
