package org.pawelsadlo2
package calculations

import calculations.CoinGekoRatesConversion.Quotation

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.pawelsadlo2.calculations.ForecastCalculator.{ExchangeForecast, ExchangeRequest, SingleForecast}
import org.pawelsadlo2.httprequests.{GetRequest, SimpleGETRequest}
import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser
import spray.json.DefaultJsonProtocol

import scala.concurrent.ExecutionContext

object ForecastCalculator {
  def apply(request: GetRequest) = new ForecastCalculator(request)

  final case class SingleForecast(rate: BigDecimal, amount: BigDecimal, result: BigDecimal, fee: BigDecimal)

  final case class ExchangeForecast(from: String, to: Map[String, SingleForecast]) //probably will not need it

  type ExchangeForecastT = Array[Any]

  final case class ExchangeRequest(from: String, to: Array[String], amount: BigDecimal)


}

class ForecastCalculator(request: GetRequest) {
  def calculate(requestData: ExchangeRequest) = {
    implicit val executionContext = ExecutionContext.global
    val provision = BigDecimal(0.0001)

    def ratesData = request.responseBody().map(CoinGekoResponseStringParser().parse)

    val fee = requestData.amount * provision

    def exchangeAfterFee(rate:BigDecimal) = (requestData.amount-fee)*rate

    def mapRate(name: String, rate: BigDecimal) = {
      (name, SingleForecast(rate, requestData.amount, exchangeAfterFee(rate), fee))
    }

    def quotationToExchangeForecast(quotation: Quotation)
    = ExchangeForecast(
      quotation.source,
      quotation.rates.map { case (k, v) => mapRate(k, v) }
    )

    ratesData
      .map(
        rates => CoinGekoRatesConversion().calculateRates(requestData.from, Option(requestData.to.toList), rates)
      ).map(
      quotationToExchangeForecast
    )
  }


}
