package us.oyanglul.zhuyu
package jobs

import effects._
import shapeless._
import cats.effect.IO
import cats.data.Kleisli

trait Job[A, -Deps] {
  def liftF[F[_], B](fa: F[B]): Kleisli[F, Deps, B] = Kleisli.liftF(fa)
  def distribute(a: A): Kleisli[IO, Deps, Unit]
  def spread[C >: A] =
    new SafeSendMessage[C, A]
}

object Job {
  def apply[E, Has](implicit job: Job[E, Has]) = job

  implicit def cnilJob[Has]: Job[CNil, Has] = new Job[CNil, Has] {
    def distribute(event: CNil) =
      throw new Exception("Inconceivable!")
  }

  implicit def coproductJob[Has, Head, Tail <: Coproduct](
      implicit
      hSeeker: Lazy[Job[Head, Has]],
      tSeeker: Job[Tail, Has]
  ): Job[Head :+: Tail, Has] = new Job[Head :+: Tail, Has] {
    def distribute(event: Head :+: Tail) = event match {
      case Inl(head) => hSeeker.value.distribute(head)
      case Inr(tail) => tSeeker.distribute(tail)
    }
  }

  implicit def genericJob[Has, A, Repr](
      implicit
      gen: Generic.Aux[A, Repr],
      job: Lazy[Job[Repr, Has]]
  ): Job[A, Has] = new Job[A, Has] {
    def distribute(event: A) = job.value.distribute(gen.to(event))
  }
}
