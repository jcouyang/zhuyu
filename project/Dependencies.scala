import sbt._

object Dependencies {
  lazy val cats = Seq(
    "org.typelevel"   %% "cats-core" % "2.7.0",
    "org.typelevel" %% "cats-effect" % "3.3.11",
  )

  lazy val doobie = Seq(
    "org.tpolecat" %% "doobie-core" % "0.13.4",
  )
  lazy val shapeless = Seq(
    "com.chuusai" %% "shapeless" % "2.3.9"
  )
  lazy val fs2 = Seq(
    "co.fs2" %% "fs2-io" % "1.0.4"
  )

  lazy val http4s = {
    val http4sVersion = "0.23.11"
    Seq(
    "org.http4s" %% "http4s-dsl" % http4sVersion,
    "org.http4s" %% "http4s-client" % http4sVersion
    )
  }

  lazy val awsSqs = Seq(
    "com.amazonaws" % "aws-java-sdk-sqs" % "1.12.219",
    "com.amazonaws" % "amazon-sqs-java-temporary-queues-client" % "2.0.0"
  )
  lazy val awsS3 = Seq(
    "com.amazonaws" % "aws-java-sdk-s3" % "1.12.218"
  )
  lazy val ciris = Seq(
    "is.cir"          %% "ciris-cats"          % "0.12.1",
    "is.cir"          %% "ciris-cats-effect"   % "0.12.1",
    "is.cir"          %% "ciris-core"          % "0.12.1",
    "is.cir"          %% "ciris-enumeratum"    % "0.12.1",
  )
  lazy val circe = {
    val version = "0.14.1"
    Seq(
    "io.circe" %% "circe-core" % version,
    "io.circe" %% "circe-generic" % version,
    "io.circe" %% "circe-parser" % version
  )
  }

  lazy val specs2 = Seq(
    "org.specs2"      %% "specs2-core"         % "4.6.0" % "it,test",
    "org.specs2"      %% "specs2-scalacheck"   % "4.6.0" % "it,test",
    "org.specs2"      %% "specs2-mock"   % "4.6.0" % "it,test",
  )

  lazy val log4s = Seq(
    "org.log4s" %% "log4s" % "1.10.0"
  )

  lazy val logback = Seq(
    "ch.qos.logback"  %  "logback-classic"     % "1.2.11",
  )

  lazy val scalacheckShapeless = Seq(
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % "1.2.0" % "it,test"
  )

  lazy val scalacheck = Seq(
    "org.scalamock"   %% "scalamock" % "4.1.0" % "it,test",
  )
}
