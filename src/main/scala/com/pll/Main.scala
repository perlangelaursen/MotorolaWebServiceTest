import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives
import akka.stream.scaladsl.Flow
import common.{Item, JsonSupport}
import scala.io.StdIn
import scala.concurrent.{Future, Await}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl._
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._

case class Radio(alias : String, allowed_locations : Seq[String])
case class Location(location : String)

object Main extends App with Directives with JsonSupport {
  implicit val system = ActorSystem("MotorolaRadio")
  implicit val materializer = ActorMaterializer()

  val db = Database.forConfig("h2mem")
  try {
    val radios = TableQuery[Radios]
    val locations = TableQuery[Locations]

    val setup = DBIO.seq((radios.schema ++ locations.schema).create)
    val setupFuture = db.run(setup)
    Await.result(setupFuture, Duration.INF)

    val route =
      pathPrefix("radios"/ IntNumber) { id =>
        pathEnd {
          post {
            entity(as[Radio]) { radio =>
              radios += (id, radio.alias, None)
              val insertLocations = locations ++ (radios.allowed_locations.map((l) => {
                (id, l)
              }))
              db.run(insertLocations.result)
              complete(StatusCodes.OK)
            }
          }
        } ~ path("location") {
          post {
            entity(as[Location]) { loc =>
              val checkIfLocationIsValid = locations.filter(_.id == id && _.location == loc)
              val checkResult = db.run(checkIfLocationIsValid.result).futureValue
              if (checkResult.size == 1) {
                val updateLocation = radios.filter(_.id == id).map(_.location)
                val updateAction = updateLocation.update(Some(loc))
                db.run(updateAction.result)
                complete(StatusCodes.OK)
              } else {
                complete(403 -> "Forbidden location")
              }
            }
          } ~ get {
            val findLocation = radios.filter(_.id == id).map(_.location)
            val findResult = db.run(findLocation.result).futureValue
            if (findResult.size == 1 && !(findResult.head.isEmpty)) {
              complete(Location(findResult.head.get))
            } else {
              complete(404 -> "NOT FOUND")
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
    bindingFuture.onFailure {
      case e:Exception => println(s"$e Failed to bind to localhost at port 8080")
    }
    println(s"Server location: http://localhost:8080/\nPress RETURN to stop the server")
    StdIn.readLine()
    bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate(1))
  } finally db.close
}
