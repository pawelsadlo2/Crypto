package org.pawelsadlo2

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Directives, Route}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.pawelsadlo2.calculations.CoinGekoRatesConversion.Quotation
import org.pawelsadlo2.calculations.{CoinGekoRatesConversion, ForecastCalculator}
import org.pawelsadlo2.calculations.ForecastCalculator.ExchangeRequest
import org.pawelsadlo2.httprequests.SimpleGETRequest
import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser
import spray.json._

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.StdIn

object Main extends App {

  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "my-system")
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext: ExecutionContextExecutor = system.executionContext

  import Endpoints._

  val bindingFuture = Http().newServerAt("localhost", 8080).bind(routes)
  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
