import akka.NotUsed
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.headers.`Content-Type`
import akka.stream.scaladsl.Flow
import scala.io.StdIn
import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream._
import akka.stream.scaladsl._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import slick.backend.DatabasePublisher
import slick.driver.H2Driver.api._
import scala.concurrent.ExecutionContext.Implicits.global

import com.fasterxml.jackson.databind.{ObjectMapper, DeserializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

object JsonMapper {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(value : Any) = {
    mapper.writeValueAsString(value)
  }

  def convert[Type](jsonString : String)(implicit m : Manifest[Type]) = {
    mapper.readValue[Type](jsonString)
  }
}

case class Radio(alias : String, allowed_locations : Seq[String])
case class Location(location : String)

object Main extends App with Directives {
  implicit val system = ActorSystem("MotorolaRadio")
  implicit val materializer = ActorMaterializer()

  val db = Database.forConfig("h2mem")
  val radios = TableQuery[Radios]
  val locations = TableQuery[Locations]

  val setup = DBIO.seq((radios.schema ++ locations.schema).create)
  val setupFuture = db.run(setup)
  Await.result(setupFuture, Duration.Inf)

  val route =
    pathPrefix("radios"/ IntNumber) { id =>
      pathEnd {
        post {
          entity(as[String]) { payload =>
            val radio = JsonMapper.convert[Radio](payload)
            radios += (id, radio.alias, None)
            locations ++= (radio.allowed_locations.map((l) => {
              (id, l)
            }))
            complete(StatusCodes.OK)
          }
        }
      } ~ path("location") {
        post {
          entity(as[String]) { payload =>
            val loc = JsonMapper.convert[Location](payload)
            val checkIfLocationIsValid = locations.filter(_.rad_id === id).filter(_.location === loc.location)
            val checkResult = Await.result(db.run(checkIfLocationIsValid.result), Duration.Inf)
            if (checkResult.size == 1) {
              val updateLocation = radios.filter(_.rad_id === id).map(_.location)
              val updateAction = updateLocation.update(Some(loc.location))
              db.run(updateAction)
              complete(StatusCodes.OK)
            } else {
              complete(403 -> "Forbidden location")
            }
          }
        } ~ get {
          val findLocation = radios.filter(_.rad_id === id).map(_.location)
          val findResult = Await.result(db.run(findLocation.result), Duration.Inf)
          if (findResult.size == 1 && !(findResult.head.isEmpty)) {
            complete(JsonMapper.toJson(Location(findResult.head.get)))
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
  db.close
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
