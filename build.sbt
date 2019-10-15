import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "us.oyanglul"
ThisBuild / scalafmtOnCompile := true

addCompilerPlugin("org.spire-math" %% "kind-projector"     % "0.9.9")
addCompilerPlugin("com.olegpy"     %% "better-monadic-for" % "0.3.0-M4")

lazy val core = (project in file("core"))
  .settings(
    name := "zhuyu-core",
    scalacOptions --= Seq(
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:params"
    ),
    libraryDependencies ++= shapeless ++
      cats ++
      fs2 ++
      circe ++
      awsSqs ++
      log4s
  )

lazy val example = project
  .settings(
    name := "zhuyu-example",
    libraryDependencies ++= logback
  )
  .dependsOn(core, `effect-http4s`, `effect-s3`)

lazy val `effect-http4s` = project
  .settings(
    name := "zhuyu-effect-doobie",
    libraryDependencies ++= http4s
  )
  .dependsOn(core)

lazy val `effect-s3` = project
  .settings(
    name := "zhuyu-effect-s3",
    libraryDependencies ++= awsS3
  )
  .dependsOn(core)

