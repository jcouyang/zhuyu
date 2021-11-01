import Dependencies._

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
    pgpPublicRing := file("/home/circleci/repo/.gnupg/pubring.asc"),
    pgpSecretRing := file("/home/circleci/repo/.gnupg/secring.asc"),
    releaseEarlyWith := SonatypePublisher,
  )
)
ThisBuild / crossScalaVersions := Seq("2.12.15", "2.13.7")
ThisBuild / scalaVersion     := "2.12.15"
ThisBuild / organization     := "us.oyanglul"
ThisBuild / scalafmtOnCompile := true

addCompilerPlugin("org.typelevel" %% "kind-projector"     % "0.10.3")
addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.1")

lazy val core = project
  .settings(
    name := "zhuyu",
    scalacOptions --= Seq(
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:params"
    ),
    libraryDependencies ++=
      shapeless ++
      cats ++
      circe ++
      awsSqs ++
      log4s
  )

lazy val example = project
  .settings(
    name := "zhuyu-example",
    libraryDependencies ++=
      fs2 ++
      logback ++
      http4s ++ Seq(
      "org.http4s" %% "http4s-blaze-client" % "0.20.11",
      "org.tpolecat" %% "doobie-postgres" % "0.13.4",
    ) ++ doobie
  )
  .dependsOn(core, `effect-http4s`, `effect-s3`, `effect-doobie`)

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
