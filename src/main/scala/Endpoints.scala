package org.pawelsadlo2

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.Done
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import org.pawelsadlo2.calculations.ForecastCalculator.ExchangeRequest
import org.pawelsadlo2.calculations.{ForecastCalculator, JsonSupport}
import org.pawelsadlo2.httprequests.SimpleGETRequest
import spray.json.DefaultJsonProtocol._

import scala.io.StdIn
import scala.concurrent.{ExecutionContext, Future}

object Endpoints extends JsonSupport {

  val geckoRequest = SimpleGETRequest("https://api.coingecko.com/api/v3/exchange_rates")
  val routes: Route =
    pathPrefix("currencies") {
      concat(
        path("exchange") {
          post {
            entity(as[ExchangeRequest]) { request =>
              onSuccess(ForecastCalculator(geckoRequest).calculate(request)) { performed =>
                complete(performed)
              }
            }
          }
        },
        path(Segment) { currency =>
          parameters("filter".repeated) { filters =>
            get {
              onSuccess(GetRates.convert(currency, filters.toList match { case Nil => None; case _ => Some(filters.toList) })) {
                x => complete(x)
              }
            }
          }
        })
    }
}
