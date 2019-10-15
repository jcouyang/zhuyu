package us.oyanglul.zhuyu
package effects

import cats.data.Kleisli
import cats.effect.IO
import com.amazonaws.services.s3.AmazonS3

trait HasS3 {
  val s3Client: AmazonS3
}

object S3 {
  def apply[A](run: AmazonS3 => A): Kleisli[IO, HasS3, A] = Kleisli { has =>
    IO.delay(run(has.s3Client))
  }
}
