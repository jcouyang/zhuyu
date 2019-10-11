package us.oyanglul.zhuyu

import shapeless._
import scala.annotation.implicitNotFound

trait Index[A, C <: Coproduct] {
  type N <: Nat
  def value(implicit n: ops.nat.ToInt[N]) = n.apply()
}
object Index {
  type Aux[A, C <: Coproduct, R <: Nat] = Index[A, C] { type N = R }
  def apply[A, C <: Coproduct](
      implicit index: Index[A, C]): Aux[A, C, index.N] = index
  implicit def found[A, C <: Coproduct]: Aux[A, A :+: C, Nat._0] =
    new Index[A, A :+: C] {
      type N = Nat._0
    }

  implicit def notFound[A, C <: Coproduct, H, Next <: Nat](
      implicit next: Aux[A, C, Next],
      e: A =:!= H
  ): Aux[A, H :+: C, Succ[Next]] = new Index[A, H :+: C] {
    type N = Succ[Next]
  }
}
trait HasOrder[+A] {
  type Order <: Coproduct
}
object HasOrder {
  type Aux[A, R] = HasOrder[A] { type Order = R }
}

@implicitNotFound(
  "Implicit not found: send Message from ${A} to ${B} will cause cycle loop, please check the order of ${A} and ${B} in ${C}")
trait CycleBreaker[C, A <: C, B <: C]
object CycleBreaker {
  implicit def allow[C, A <: C, B <: C, R <: Coproduct, NA <: Nat, NB <: Nat](
      implicit ord: HasOrder.Aux[C, R],
      indexA: Index.Aux[A, R, NA],
      indexB: Index.Aux[B, R, NB],
      na: ops.nat.ToInt[NA],
      nb: ops.nat.ToInt[NB],
      evidenceOfALittleThanB: ops.nat.LT[NA, NB]
  ): CycleBreaker[C, A, B] = new CycleBreaker[C, A, B] {}
}
