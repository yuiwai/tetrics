import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scalapb.compiler.Version.scalapbVersion

scalaVersion in ThisBuild := "2.12.7"
version in ThisBuild := "0.1.0"

val pbruntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Full) in file("core"))
  .settings(
    name := "tetrics-core",
    PB.protoSources in Compile := Seq((baseDirectory in ThisBuild).value / "proto"),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .jvmSettings(
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.5" % "test"
  )
  .jsSettings(
    libraryDependencies += pbruntime
  )
  .nativeSettings(
    scalaVersion := "2.11.11",
    libraryDependencies += pbruntime
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val js = (project in file("js"))
  .settings(
    name := "tetrics-js",
    scalaJSUseMainModuleInitializer := true,
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "0.9.2"
  )
  .dependsOn(coreJS)
  .enablePlugins(ScalaJSPlugin)

lazy val native = (project in file("native"))
  .settings(
    name := "tetrics-native",
    scalaVersion := "2.11.11"
  )
  .dependsOn(coreNative)
  .enablePlugins(ScalaNativePlugin)

lazy val libgdx = (project in file("libgdx"))
  .settings(
    name := "tetrics-gdx",
    resolvers += "scala-sapporo repo" at "https://s3-us-west-2.amazonaws.com/repo.scala-sapporo.org",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.2",
      "org.scalasapporo.gamecenter" %% "scala-gamecenter-connector" % "0.1.0-SNAPSHOT",
      "com.softwaremill.sttp" %% "async-http-client-backend-future" % "1.3.7",
      "com.badlogicgames.gdx" % "gdx-backend-lwjgl" % "1.9.8",
      "com.badlogicgames.gdx" % "gdx-platform" % "1.9.8" classifier "natives-desktop"
    )
  )
  .dependsOn(coreJVM)

lazy val server = (project in file("server"))
  .settings(
    name := "tetrics-server",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.5",
      "com.typesafe.akka" %% "akka-stream" % "2.5.12"
    )
  )
  .dependsOn(coreJVM)