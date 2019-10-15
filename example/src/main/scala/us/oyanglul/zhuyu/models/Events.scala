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

case class PaymentInited(id: Int) extends Event
case class PaymentDebited(status: Int) extends Event
case class DebitEntryFileUploaded(etag: String) extends Event
case class BillingServiceNotified(id: Int) extends Event
case class DebitEntryProcessed(id: Int) extends Event
