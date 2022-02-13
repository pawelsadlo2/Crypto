package org.pawelsadlo2
package calculations

import calculations.CoinGekoRatesConversion.Quotation
import calculations.ForecastCalculator.{ExchangeForecast, ExchangeRequest, SingleForecast}

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit val quotationFormat = jsonFormat2(Quotation)
  implicit val singleForecastFormat = jsonFormat4(SingleForecast)
  implicit val exchangeForecastFormat = jsonFormat2(ExchangeForecast)
  implicit val forecastRequestFormat = jsonFormat3(ExchangeRequest)
}
