package org.pawelsadlo2

import calculations.{CoinGekoRatesConversion, ForecastCalculator}
import calculations.ForecastCalculator.ExchangeRequest
import httprequests.SimpleGETRequest
import parsing.coinGeko.CoinGekoResponseStringParser

import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser.CoinGekoRatesFormat

import scala.concurrent.{ExecutionContext, Future}

object GetRates {
  private[this] implicit val executionContext=ExecutionContext.global
  private val request = SimpleGETRequest("https://api.coingecko.com/api/v3/exchange_rates")


  private def ratesData = request.responseBody().map(CoinGekoResponseStringParser().parse)

  def convert[CC<:Iterable[String]](source:String,targets:Option[CC]): Future[CoinGekoRatesConversion.Quotation] = ratesData.map(x => CoinGekoRatesConversion().calculateRates(source, targets,x))

}
