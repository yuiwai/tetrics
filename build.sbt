import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}
import scalapb.compiler.Version.scalapbVersion

scalaVersion in ThisBuild := "2.12.7"
version in ThisBuild := "0.1.0"

val pbruntime = "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf"

lazy val core = (crossProject(JSPlatform, JVMPlatform, NativePlatform).crossType(CrossType.Pure) in file("core"))
  .settings(
    name := "tetrics-core",
    PB.protoSources in Compile := Seq((baseDirectory in ThisBuild).value / "proto"),
    PB.targets in Compile := Seq(
      scalapb.gen() -> (sourceManaged in Compile).value
    )
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