import mill._
import scalalib._
import scalajslib._

object core extends ScalaModule with ScalaJSModule {
  def scalaVersion = "2.12.6"
  override def scalaJSVersion = "0.6.24"
}

object js extends ScalaJSModule {
  def scalaVersion = "2.12.6"
  override def scalaJSVersion = "0.6.24"
  override def moduleDeps = Seq(core)
  override def ivyDeps = Agg(
    ivy"org.scala-js::scalajs-dom::0.9.2"
  )
  override def fastOpt = T {
    import ammonite.ops._
    val p = super.fastOpt()
    val d = pwd / "js" / "tetrics.js"
    cp.over(p.path, d)
    PathRef(d)
  }
}
