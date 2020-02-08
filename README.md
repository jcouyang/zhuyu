<h1><ruby>鸀鳿<rt>zhu yu</rt></ruby></h1>

<img src=https://upload.wikimedia.org/wikipedia/commons/2/2e/Imperial_Encyclopaedia_-_Animal_Kingdom_-_pic061_-_%E9%B8%80%E9%B3%BF%E5%9C%96.svg height="250px" align="right" />

Typed, Functional Scala SQS Consumer and Composable Functional Effects

[![CircleCI](https://circleci.com/gh/jcouyang/zhuyu.svg?style=svg)](https://circleci.com/gh/jcouyang/zhuyu)
[![Latest version](https://index.scala-lang.org/jcouyang/zhuyu/latest.svg?color=orange&v=1)](https://index.scala-lang.org/jcouyang/zhuyu)
[![javadoc](https://javadoc.io/badge2/us.oyanglul/zhuyu-core_2.12/0.2.0/javadoc.svg)](https://javadoc.io/doc/us.oyanglul/zhuyu-core_2.12/0.2.0) \
![Cats Friendly Badge](https://typelevel.org/cats/img/cats-badge-tiny.png) 

*Do one thing and do it well* micro [birds library](https://github.com/search?q=org:jcouyang+topic:birds&type=Repositories) series

ZhuyuVersion = [![Latest version](https://index.scala-lang.org/jcouyang/zhuyu/latest.svg?color=orange&v=1)](https://index.scala-lang.org/jcouyang/zhuyu)

```
libraryDependencies += "us.oyanglul" %% "zhuyu" % ZhuyuVersion
```

# Get Started

You can quickly scaffold a zhuyu project by using [jcouyang/zhuyu.g8](https://github.com/jcouyang/zhuyu.g8)

```sh
sbt new jcouyang/zhuyu.g8
```

Once you have the project, let us start form a simple example.

Before we dive into the example, there are some glossaries(types) you need to notice:

<img src=https://user-images.githubusercontent.com/1235045/74084487-b12fcf00-4ac3-11ea-9890-09444c1a3f1d.png width=70%/>

1. `Envelop[A]`: model of message in SQS
2. `Job[A, Deps]`: Job description for `A`, requires `Deps`
3. `Worker[A, Deps]`: provided `Deps` and `Job[A,Deps]` exists, worker is able to work on the job.

## Example
Say we have a long work flow, each step is safe to retry(idempotent) without notice user
Ideally we could put all tasks from the work flow into sqs to guarantee each of them will finish eventually.

Here is the work flow:
1. Init Payment
2. Debit Payment
3. Notify User Payment Result
4. Prepare Order

1 and 2 have to finish by sequence, but 3 and 4 can be done at the same time.

### 1. Define Events
```scala
sealed trait Event
case class InitPayment(id: Int) extends Event
case class DebitPayment(id: Int, cardnum: String) extends Event
case class NotifyPaymentResult(result: Int) extends Event
case class PrepareOrder(id: Int) extends Event
```

### 2. Create a Job

Let us start implement the tasks, or shall we call them `Job`s

It is good to have nice convension so the implementation will be much more predictable.

So we can simply prefix **On** i.e. **On**InitPayment


```scala
import effects._
trait OnInitPayment {
  implicit val onInitPayment =
    new Job[InitPayment, HasSQS with HasDoobie] {             // <- (1)
      override def distribute(message: Envelop[InitPayment]) =
        for {
          cardnum <- Doobie(sql"select cardnum from credit_card where id = ${message.content.id}".query[String].unique)
          _ <- spread[Event](DebitPayment(cardnum))           // <- (2)
        } yield ()
    }
}
```

1. create a `Job` to handle `InitPayment` event, which requires `HasSQS` and `HasDoobie` effects
2. `spread` the `Event` of `DebitPayment(cardnum)`, the spreaded event will be `distribute`d by `Job[DebitPayment, ?]`

```
Implicit not found: send Message from us.oyanglul.zhuyu.models.InitPayment to us.oyanglul.zhuyu.models.InitPayment will cause cycle loop, please check the order of us.oyanglul.zhuyu.models.InitPayment and us.oyanglul.zhuyu.models.InitPayment in us.oyanglul.zhuyu.models.Event
[error]            _ <- spread[Event](DebitPayment(cardnum))
```
:memo: that `spread` is typelevel safe from cycle loop, which means
if you want to `spread[Event](InitPayment)` in `Job[InitPayment, HasSQS]` you have to define `HasOrder` type class `HasOrder[Event]` about order of `InitPayment` and `DebitPayment`.

```scala
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
```

`spread` event in wrong order will cause compile error(that to shapeless so we can do counting at typelevel)

### 3. Register `OnInitPayment`
Once implemented the new Job, register it in `pacakge.scala` so `Worker` knows where to look for jobs.

```scala
package object jobs
    extends OnInitPayment
    with OnDebitPayment
    with OnNotifyUser
    with OnPrepareOrder
```

### 4. Hire Workers

Now we have 4 jobs ready for pick up, where are our workers?

```scala
import jobs._
object impl extends HasSQS with HasHttp4s with HasS3 with HasDoobie {???}
Worker.work[Event, HasSQS with HasHttp4s with HasS3 with HasDoobie].run(impl)
```

everytime our Worker `run`:
1. Worker will start polling `Event` from sqs
2. find out what `Job` the `Event` belongs to
3. work on the `Job` by instruction from `Job.distribute` method

`Worker` is type level safe as well, for any `Event` that the `Worker` cannot find correct `Job`, compile error will occur. Thus you never encounter runtime error for unprepared `Job`, all `Event` `Worker` work on will definitly have `Job` defined.


### Create more `Job`s

```scala
import effects._
trait OnDebitPayment {
  implicit val onDebitPayment =
    new Job[DebitPayment, HasSQS with HasHttp4s] {
      override def distribute(message: Envelop[DebitPayment]) =
        for {
          result <- Http4s(_.expect[Int](uri"http://payment-gateway.com/pay/${message.content.cardnum}")
          _ <- spread[Event](NotifyPaymentResult(result))
          _ <- spread[Event](PrepareOrder(message.content.id))
        } yield ()
    }
}
```

## Request Response Pattern Example
The previous example is 1 way message only, the requester put message in the queue never expect any response.

But there is some common use case of the request-response messaging pattern, where we can use AWS requester creates a temporary queue for receiving each response message[1]. 

<img src=https://user-images.githubusercontent.com/1235045/74084496-ca388000-4ac3-11ea-9544-1eba7c21643f.png width=70% />

```diff
import effects._
trait OnDebitPayment {
  implicit val onDebitPayment =
    new Job[DebitPayment, HasSQS with HasHttp4s] {
      override def distribute(message: Envelop[DebitPayment]) =
        for {
          result <- Http4s(_.expect[Int](uri"http://payment-gateway.com/pay/${message.content.cardnum}")
+         _ <- SQS.respond[Event](message.cover, ResponseEvent)
          _ <- spread[Event](NotifyPaymentResult(result))
          _ <- spread[Event](PrepareOrder(message.content.id))
        } yield ()
    }
}
```
# Optional effect modules

There are few builtin effect implementations, you can also simply create your own effect. 
Just may sure your effect has type `Kleisli[IO, HasSomething, A]`

## Http4s Client
```
libraryDependencies += "us.oyanglul" %% "zhuyu-effect-http4s" % ZhuyuVersion
```

```scala
effects.Http4s(_.status(GET(uri"https://blog.oyanglul.us")))
// Kleisli[IO, HasHttp4s, Status]
```
## S3
```
libraryDependencies += "us.oyanglul" %% "zhuyu-effect-s3" % ZhuyuVersion
```

```scala
effects.S3(_.putObject("example-bucket", "filename", "content"))
// Kleisli[IO, HasS3, PutObjectResult]
```

## Doobie
```
libraryDependencies += "us.oyanglul" %% "zhuyu-effect-doobie" % ZhuyuVersion
```

```scala
effects.Doobie(sql"select 250".query[Int].unique)
// Kleisli[IO, HasDoobie, Int]
```

All these effects can be composed

```scala
val program = for {
 _ <- effects.Http4s(_.status(GET(uri"https://blog.oyanglul.us")))
 _ <- effects.S3(_.putObject("example-bucket", "filename", "content"))
 _ <- effects.Doobie(sql"select 250".query[Int].unique)
} yield()
// Kleisli[IO, HasHttp4s with HasS3 with HasDoobie, Unit]
```

to run effects simply provide impletations
```scala
  object impl
      with HasHttp4s
      with HasS3
      with HasDoobie {
      //...implement what ever compiler complains
      }
  program.run(impl) // IO[Unit]
```

for more detail, look at [example](https://github.com/jcouyang/zhuyu/tree/master/example/src/main/scala/us/oyanglul/zhuyu) `Main.scala` and `jobs`

[1]: https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-temporary-queues.html#request-reply-messaging-pattern
