package com.yuiwai.tetrics.server

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.unmarshalling.Unmarshaller
import akka.stream.ActorMaterializer
import com.yuiwai.tetrics.core.DefaultAutoPlayer
import com.yuiwai.tetrics.core.converter.ProtobufConverter

import scala.io.StdIn

object Server {
  import scala.concurrent.duration._
  def main(args: Array[String]) {
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher
    implicit val unmarshaller = Unmarshaller { implicit ec =>
      r: HttpRequest =>
        r.entity.toStrict(1.second)
          .map(r => tetrics.Request.parseFrom(r.data.toArray))
    }
    val autoPlayer = DefaultAutoPlayer()

    val route =
      pathEndOrSingleSlash {
        post {
          entity(as[tetrics.Request]) { r =>
            val actions = autoPlayer.actImpl(ProtobufConverter.fromProto(r))
            complete(HttpEntity(ProtobufConverter.toProto(actions).toByteArray))
          }
        }
      }
    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }
}
