import ammonite.ops._
import mill._, scalalib._, scalajslib._, scalanativelib._

trait Base extends ScalaModule {
  def scalacOptions = Seq(
    "-unchecked",
    "-deprecation",
    "-encoding", "utf8",
    "-feature"
  )
}
trait TetricsModule extends CrossSbtModule with Base {
  override def millSourcePath: Path = pwd / "core"
}
object tetricsJvm extends Cross[TetricsJvmModule]("2.11.12", "2.12.6")
class TetricsJvmModule(val crossScalaVersion: String) extends TetricsModule {
}
object auto extends ScalaModule with Base {
  override def moduleDeps: Seq[JavaModule] = Seq(tetricsJvm("2.12.6"))
  override def scalaVersion = "2.12.6"
}
object check extends ScalaModule with Base {
  override def moduleDeps: Seq[JavaModule] = Seq(tetricsJvm("2.12.6"))
  override def scalaVersion = "2.12.6"
  object test extends Tests {
    def ivyDeps = Agg(ivy"com.lihaoyi::utest::0.6.5")
    def testFrameworks = Seq("utest.runner.Framework")
  }
}

object tetricsJs extends Cross[TetricsJsModule]("2.11.12", "2.12.6")
class TetricsJsModule(val crossScalaVersion: String) extends TetricsModule with ScalaJSModule {
  override def scalaJSVersion = "0.6.24"
}
object tetricsNative extends Cross[TetricsNativeModule]("2.11.12")
class TetricsNativeModule(val crossScalaVersion: String) extends TetricsModule with ScalaNativeModule {
  def scalaNativeVersion = "0.3.8"
}

object js extends ScalaJSModule with SbtModule with Base {
  override def scalaVersion: T[String] = "2.12.6"
  override def scalaJSVersion = "0.6.24"
  override def moduleDeps = Seq(tetricsJs("2.12.6"))
  override def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::0.9.2"
  )
  override def fastOpt = T {
    import ammonite.ops._
    val p = super.fastOpt()
    val d = pwd / "js" / "contents" / "tetrics.js"
    cp.over(p.path, d)
    PathRef(d)
  }
}

object native extends ScalaNativeModule with SbtModule with Base {
  def scalaVersion = "2.11.12"
  def scalaNativeVersion = "0.3.8"
  override def moduleDeps = Seq(tetricsNative("2.11.12"))
}