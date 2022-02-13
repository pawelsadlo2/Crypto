package org.pawelsadlo2
package httprequests

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.unmarshalling.Unmarshal

import scala.concurrent.{ExecutionContextExecutor, Future}
import akka.http.scaladsl.model.HttpMethods.GET

object SimpleGETRequest {
  def apply(endpoint: String) = new SimpleGETRequest(endpoint)
}

class SimpleGETRequest(val endpoint: String) extends GetRequest {
  private implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "SingleRequest")
  // needed for the future flatMap/onComplete in the end
  private implicit val executionContext: ExecutionContextExecutor = system.executionContext


  private def sendRequest(URI: String) = {
    Http().singleRequest(HttpRequest(method = GET, uri = URI))
  }

  private def unmarshalResponse(response: HttpResponse) = {
    response.status match {
      case OK => Unmarshal(response.entity).to[String]
      case _ => ???
    }
  }

  def responseBody(): Future[String] = for {
    x <- sendRequest(endpoint)
    n <- unmarshalResponse(x)
  } yield n

}
