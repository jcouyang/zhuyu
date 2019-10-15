package us.oyanglul.zhuyu
package impls

import cats.effect.{ContextShift, IO}
import org.http4s.client.blaze.BlazeClientBuilder

import scala.concurrent.ExecutionContext.Implicits.global
trait HasHttp4sImpl extends effects.HasHttp4s {
  implicit val cs: ContextShift[IO] = IO.contextShift(global)
  val http4sClient =
    BlazeClientBuilder[IO](global).resource.allocated.unsafeRunSync()._1
}
