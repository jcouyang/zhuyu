package us.oyanglul.zhuyu
package jobs

import effects._
import shapeless._
import cats.effect.IO
import cats.data.Kleisli

trait Job[A, -Deps] {
  def liftF[F[_], B](fa: F[B]): Kleisli[F, Deps, B] = Kleisli.liftF(fa)
  def distribute(a: Envelop[A]): Kleisli[IO, Deps, Unit]
  def spread[C >: A] =
    new SafeSendMessage[C, A]
}

object Job {
  def apply[E, Has](implicit job: Job[E, Has]) = job

  implicit def cnilJob[Has]: Job[CNil, Has] = new Job[CNil, Has] {
    def distribute(event: Envelop[CNil]) =
      throw new Exception("Inconceivable!")
  }

  implicit def coproductJob[Has, Head, Tail <: Coproduct](
      implicit
      hSeeker: Lazy[Job[Head, Has]],
      tSeeker: Job[Tail, Has]
  ): Job[Head :+: Tail, Has] = new Job[Head :+: Tail, Has] {
    def distribute(envelop: Envelop[Head :+: Tail]) = envelop match {
      case Envelop(cover, Inl(head)) =>
        hSeeker.value.distribute(Envelop(cover, head))
      case Envelop(cover, Inr(tail)) =>
        tSeeker.distribute(Envelop(cover, tail))
    }
  }

  implicit def genericJob[Has, A, Repr](
      implicit
      gen: Generic.Aux[A, Repr],
      job: Lazy[Job[Repr, Has]]
  ): Job[A, Has] = new Job[A, Has] {
    def distribute(envelop: Envelop[A]) =
      job.value.distribute(envelop.copy(content = gen.to(envelop.content)))
  }
}
