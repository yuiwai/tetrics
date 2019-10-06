import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scalapb.compiler.Version.scalapbVersion

scalaVersion in ThisBuild := "2.12.8"
version in ThisBuild := "0.4.0-SNAPSHOT"
organization in ThisBuild := "com.yuiwai"

val pbruntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"

lazy val root = project
  .in(file("."))
  .aggregate(coreJVM, coreJS)
  .settings(
    name := "tetrics",
    publish / skip := true
  )

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Full) in file("core"))
  .settings(
    name := "tetrics-core",
    crossScalaVersions := Seq(scalaVersion.value, "2.11.11"),
    publishTo := Some(Resolver.file("file", file("release"))),
    testFrameworks += new TestFramework("utest.runner.Framework"),
  )
  .jvmSettings(
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.5" % "test"
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      pbruntime,
      "com.lihaoyi" %%% "utest" % "0.6.5" % "test"
    )
  )
  .nativeSettings(
    scalaVersion := "2.11.11",
    crossScalaVersions := Seq("2.11.11"),
    libraryDependencies ++= Seq(
      pbruntime,
      "com.lihaoyi" %%% "utest" % "0.6.5" % "test"
    )
  )

lazy val coreJVM = core.jvm
lazy val coreJS = core.js
lazy val coreNative = core.native

lazy val pb = (crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure) in file("pb"))
  .settings(
    name := "tetrics-pb",
    PB.protoSources in Compile := Seq((baseDirectory in ThisBuild).value / "proto"),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    ),
  )
  .dependsOn(core)

lazy val pbJVM = pb.jvm
lazy val pbJS = pb.js
lazy val pbNative = pb.native

lazy val ui = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("ui"))
  .settings(
    name := "tetrics-ui",
    resolvers += "yuiwai repo" at "https://s3-us-west-2.amazonaws.com/repo.yuiwai.com"
  )
  .jvmSettings(
    libraryDependencies ++= Seq(
      "com.yuiwai" %% "yachiyo-ui" % "0.2.2-SNAPSHOT"
    )
  )
  .jsSettings(
    libraryDependencies ++= Seq(
      "com.yuiwai" %%% "yachiyo-ui" % "0.2.2-SNAPSHOT"
    )
  )
  .dependsOn(core)

lazy val uiJVM = ui.jvm
lazy val uiJS = ui.js

lazy val check = (project in file("check"))
  .settings(
    libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.5" % "test",
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .dependsOn(coreJVM)

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
  .dependsOn(coreNative, appNative)
  .enablePlugins(ScalaNativePlugin)

lazy val libgdx = (project in file("libgdx"))
  .settings(
    name := "tetrics-gdx",
    libraryDependencies ++= Seq(
      "com.typesafe" % "config" % "1.3.2",
      "com.softwaremill.sttp" %% "akka-http-backend" % "1.3.8",
      "com.typesafe.akka" %% "akka-stream" % "2.5.11",
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

lazy val pwa = (project in file("pwa"))
  .settings(
    name := "tetrics-pwa",
    resolvers += "yuiwai repo" at "https://s3-us-west-2.amazonaws.com/repo.yuiwai.com",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "utest" % "0.6.5" % "test",
      "com.github.japgolly.scalajs-react" %%% "core" % "1.4.1",
      "com.github.japgolly.scalajs-react" %%% "extra" % "1.4.1",
      "com.yuiwai" %%% "yachiyo-zio" % "0.2.2-SNAPSHOT"
    ),
    scalaJSUseMainModuleInitializer := true,
    npmDependencies in Compile ++= Seq(
      "react" -> "16.5.1",
      "react-dom" -> "16.5.1")
  )
  .enablePlugins(ScalaJSBundlerPlugin)
  .dependsOn(coreJS, uiJS)

lazy val cli = (project in file("cli"))
  .settings(
    name := "tetrics-cli"
  )
  .dependsOn(appJVM)

lazy val app = crossProject(JVMPlatform, JSPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("app"))
  .settings(
    name := "tetrics-app"
  )
  .nativeSettings(
    scalaVersion := "2.11.11"
  )
  .dependsOn(core)
lazy val appJVM = app.jvm
lazy val appJS = app.js
lazy val appNative = app.native

lazy val experimental = (project in file("experimental"))
  .settings(
    name := "tetrics-experimental"
  )