import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow
import common.{Item, JsonSupport}
import scala.io.StdIn
import scala.concurrent.Future
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl._

object Main extends App with Directives with JsonSupport {
  implicit val system = ActorSystem("MotorolaRadio")
  implicit val materializer = ActorMaterializer()

  val route =
    pathPrefix("radios"/ IntNumber) { id =>
      pathEnd {
        post {

        }
      } ~ path("location") {
        post {

        } ~ get {

        }
      }
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  bindingFuture.onFailure {
    case e:Exception => println(s"$ex Failed to bind to localhost at port 8080")
  }
  println(s"Server location: http://localhost:8080/\nPress RETURN to stop the server")
  StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
