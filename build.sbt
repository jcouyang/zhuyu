import Dependencies._

val dotty = "0.23.0-RC1"
val scala213 = "2.13.1"
lazy val supportedScalaVersions = List(dotty, scala213)

inScope(Scope.GlobalScope)(
  List(
    organization := "us.oyanglul",
    licenses := Seq("Apache License 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    homepage := Some(url("https://github.com/jcouyang/zhuyu")),
    developers := List(
      Developer("jcouyang", "Jichao Ouyang", "oyanglulu@gmail.com", url("https://github.com/jcouyang"))
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/jcouyang/zhuyu"),
        "scm:git@github.com:jcouyang/zhuyu.git"
      )
    ),
    pgpPublicRing := file(".") / ".gnupg" / "pubring.asc",
    pgpSecretRing := file(".") / ".gnupg" / "secring.asc",
    releaseEarlyWith := SonatypePublisher,
    scalaVersion := dotty
  )
)

lazy val core = project
  .settings(
    name := "zhuyu",
    scalacOptions ++= Seq("-language:implicitConversions"),
    scalacOptions in Test -= "-Xfatal-warnings",
    crossScalaVersions := supportedScalaVersions,
    libraryDependencies ++=
      shapeless ++
      cats ++
      circe ++
      awsSqs ++
      logger
  )

// lazy val example = project
//   .settings(
//     name := "zhuyu-example",
//     scalacOptions ++= Seq("-language:implicitConversions", "-Xignore-scala2-macros"),
//     scalacOptions in Test -= "-Xfatal-warnings",
//     crossScalaVersions := supportedScalaVersions,
//     libraryDependencies ++=
//       fs2 ++
//       logback ++
//       http4s ++ Seq(
//       "org.http4s" % "http4s-blaze-client_2.13" % "0.21.0",
//       "org.tpolecat" % "doobie-postgres_2.13" % "0.8.4",
//     ) ++ doobie
//   )
//   .dependsOn(core, `effect-http4s`, `effect-s3`, `effect-doobie`)

lazy val `effect-http4s` = project
  .settings(
    name := "zhuyu-effect-http4s",
    libraryDependencies ++= http4s
  )
  .dependsOn(core)

lazy val `effect-s3` = project
  .settings(
    name := "zhuyu-effect-s3",
    libraryDependencies ++= awsS3
  )
  .dependsOn(core)

lazy val `effect-doobie` = project
  .settings(
    name := "zhuyu-effect-doobie",
    libraryDependencies ++= doobie
  )
  .dependsOn(core)
