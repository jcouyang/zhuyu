package us.oyanglul.zhuyu
package models

import shapeless._

sealed trait Event
object Event {
  type EventOrder =
    PaymentInited :+:
      PaymentDebited :+:
      DebitEntryFileUploaded :+:
      BillingServiceNotified :+:
      DebitEntryProcessed :+: CNil
  implicit val orderedEvent: HasOrder.Aux[Event, EventOrder] =
    new HasOrder[Event] {
      type Order = EventOrder
    }
}

case class PaymentInited(batchId: Int) extends Event
case class PaymentDebited(batchId: Int) extends Event
case class DebitEntryFileUploaded(batchId: Int) extends Event
case class BillingServiceNotified(batchId: Int) extends Event
case class DebitEntryProcessed(batchId: Int) extends Event
