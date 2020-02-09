package us.oyanglul.zhuyu
package models

import shapeless._

sealed trait Event
object Event {
  type EventOrder =
    InitPayment :+:
      DebitPayment :+:
      NotifyPaymentResult :+:
      PrepareOrder :+: CNil
  implicit val orderedEvent: HasOrder.Aux[Event, EventOrder] =
    new HasOrder[Event] {
      type Order = EventOrder
    }
}

case class InitPayment(id: Int) extends Event
case class DebitPayment(status: Int) extends Event
case class NotifyPaymentResult(result: String) extends Event
case class PrepareOrder(id: Int) extends Event
