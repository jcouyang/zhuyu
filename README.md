<h1><ruby>鸀鳿<rt>zhu yu</rt></ruby></h1>

Composable Functional Effects

[![CircleCI](https://circleci.com/gh/jcouyang/zhuyu.svg?style=svg)](https://circleci.com/gh/jcouyang/zhuyu)
[![Latest version](https://index.scala-lang.org/jcouyang/zhuyu/latest.svg?color=orange&v=1)](https://index.scala-lang.org/jcouyang/zhuyu)
[![](https://www.javadoc.io/badge/us.oyanglul/zhuyu_2.12.svg?label=document)](https://www.javadoc.io/doc/us.oyanglul/zhuyu_2.12)

<img src=https://upload.wikimedia.org/wikipedia/commons/2/2e/Imperial_Encyclopaedia_-_Animal_Kingdom_-_pic061_-_%E9%B8%80%E9%B3%BF%E5%9C%96.svg width=50% />

*Do one thing and do it well* micro [birds library](https://github.com/search?q=org:jcouyang+topic:birds&type=Repositories) series

ZhuyuVersion = [![Latest version](https://index.scala-lang.org/jcouyang/zhuyu/latest.svg?color=orange&v=1)](https://index.scala-lang.org/jcouyang/zhuyu)

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

## SQS Worker
```
libraryDependencies += "us.oyanglul" %% "zhuyu-sqs-worker" % ZhuyuVersion
```

register a `Job` that will handle `PaymentInited` event in SQS

```scala
trait OnPaymentInited {
  implicit val onPaymentInited =
    new Job[PaymentInited, effects.HasSQS with effects.HasHttp4s] {
      override def distribute(message: PaymentInited) =
        for {
          status <- effects.Http4s(_.status(GET(uri"https://blog.oyanglul.us")))
          _ <- spread[Event](PaymentDebited(status.code))
        } yield ()
    }
}
```

notice that type of the job will depend on how many effects will be trigger. 
`Job[PaymentInited, effects.HasSQS with effects.HasHttp4s]` means the job will cause two effects `SQS` and `Http4s`

`spread[Event]` will put one event back to SQS, because one job should just focus on one thing, once worker finish the job, it should spread the result.

:congratulations: that `spread` is typelevel safe from cycle, which means if you send any event that could cause loop, it won't even be able to compiled(that to shapeless so we can do counting at typelevel)

for more detail, look at [example](https://github.com/jcouyang/zhuyu/tree/master/example/src/main/scala/us/oyanglul/zhuyu) `Main.scala` and `jobs`
