package org.pawelsadlo2

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.pawelsadlo2.calculations.{CoinGekoRatesConversion, ForecastCalculator, JsonSupport}
import org.pawelsadlo2.calculations.CoinGekoRatesConversion.Quotation
import org.pawelsadlo2.calculations.ForecastCalculator.{ExchangeForecast, ExchangeRequest, SingleForecast}
import org.pawelsadlo2.httprequests.GetRequest
import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser
import org.scalatest.flatspec.AnyFlatSpec
import spray.json._

import scala.concurrent.ExecutionContext

class Tests extends AnyFlatSpec with JsonSupport {

  "Quotation" should "have proper format" in {

    val quotation = Quotation("BTC", Map("USDT" -> 0.3321, "ETH" -> 0.2911))
    val quotationParsed = """{
                            |"source":"BTC",
                            |"rates":{
                            |"USDT":0.3321,
                            |"ETH":0.2911
                            |}
                            |}""".stripMargin.parseJson.convertTo[Quotation]

    assert(quotation == quotationParsed)
  }

  "ForecastRequest" should "have proper format" in {


    val forecastRequest = ExchangeRequest("currencyA", Array("currencyB", "currencyC"), 121)
    //println (forecastRequest.toJson.toString())
    val forecastParsed = """{"from":"currencyA","to":["currencyB","currencyC"],"amount":121}""".parseJson.convertTo[ExchangeRequest]
    assert(forecastRequest.toJson == forecastParsed.toJson)
  }

  "ExchangeForecast" should "have proper format" in {

    val exchangeForecast = ExchangeForecast("currencyA", Map(
      "currencyB" -> SingleForecast(0.21, 121, 0.213, 0.0001),
      "currencyC" -> SingleForecast(0.21, 121, 0.213, 0.0001)))
    val exchangeForecastParsed = """{
                                   |"from": "currencyA",
                                   |"currencyB":{
                                   |"rate":0.21,
                                   |"amount":121,
                                   |"result":0.213,
                                   |"fee":0.0001
                                   |},
                                   |"currencyC":{
                                   |"rate":0.21,
                                   |"amount":121,
                                   |"result":0.213,
                                   |"fee":0.0001
                                   |}
                                   |}""".stripMargin.parseJson.convertTo[ExchangeForecast]

    assert(exchangeForecast == exchangeForecastParsed)
  }

  "ResponseStringParser" should "not throw error" in {

    val response =
      """{
        |  "rates": {
        |    "btc": {
        |      "name": "Bitcoin",
        |      "unit": "BTC",
        |      "value": 1,
        |      "type": "crypto"
        |    },
        |    "eth": {
        |      "name": "Ether",
        |      "unit": "ETH",
        |      "value": 14.527,
        |      "type": "crypto"
        |    },
        |    "ltc": {
        |      "name": "Litecoin",
        |      "unit": "LTC",
        |      "value": 340.085,
        |      "type": "crypto"
        |    }
        |  }
        |}""".stripMargin
    assert({
      val x = CoinGekoResponseStringParser().parse(response)
      true
    })
  }


  "calculateRates" should "caltulate rates properly" in {

    val response =
      """{
        |  "rates": {
        |    "btc": {
        |      "name": "Bitcoin",
        |      "unit": "BTC",
        |      "value": 1,
        |      "type": "crypto"
        |    },
        |    "eth": {
        |      "name": "Ether",
        |      "unit": "ETH",
        |      "value": 14.527,
        |      "type": "crypto"
        |    },
        |    "ltc": {
        |      "name": "Litecoin",
        |      "unit": "LTC",
        |      "value": 340.085,
        |      "type": "crypto"
        |    }
        |  }
        |}""".stripMargin


    val data = CoinGekoResponseStringParser().parse(response)

    assert {
      CoinGekoRatesConversion().calculateRates("BTC", Option(List("BTC", "LTC")), data) == Quotation("BTC", Map("BTC" -> 1, "LTC" -> 340.085))
    }

    assert(
      CoinGekoRatesConversion().calculateRates("LTC", Option(List("BTC", "LTC")), data) == Quotation("LTC", Map("BTC" -> BigDecimal(1) / BigDecimal(340.085), "LTC" -> 1)))


    assert(
      CoinGekoRatesConversion().calculateRates("LTC", Option(List("BTC")), data) == Quotation("LTC", Map("BTC" -> BigDecimal(1) / BigDecimal(340.085))))


    assert {
      CoinGekoRatesConversion().calculateRates("BTC", Option(List("BTC")), data) == Quotation("BTC", Map("BTC" -> 1))
    }

    assert {
      CoinGekoRatesConversion().calculateRates("BTC", Option(List("BTC")), data) == Quotation("BTC", Map("BTC" -> 1))
    }

    assert {
      CoinGekoRatesConversion().calculateRates("BTC", None, data) == Quotation("BTC", Map("BTC" -> 1, "LTC" -> 340.085, "ETH" -> 14.527))
    }
  }


}

import org.scalatest.flatspec.AsyncFlatSpec
import scala.concurrent.Future

class ForecastCalculator extends AsyncFlatSpec {

  object testGETRequest extends GetRequest {
    def responseBody(): Future[String] =
      Future(
        """{
          |  "rates": {
          |    "btc": {
          |      "name": "Bitcoin",
          |      "unit": "BTC",
          |      "value": 1,
          |      "type": "crypto"
          |    },
          |    "eth": {
          |      "name": "Ether",
          |      "unit": "ETH",
          |      "value": 14.527,
          |      "type": "crypto"
          |    },
          |    "ltc": {
          |      "name": "Litecoin",
          |      "unit": "LTC",
          |      "value": 340.085,
          |      "type": "crypto"
          |    }
          |  }
          |}""".stripMargin)
  }

  "ForeCastCalculator" should "calculate BTC->BTC" in {
    val request = ExchangeRequest("BTC", Array("BTC"), 1)
    //implicit val executionContext=ExecutionContext.global
    ForecastCalculator(testGETRequest).calculate(request).map(x => assert(x == ExchangeForecast("BTC", Map("BTC" -> SingleForecast(1, 1, 0.9999, 0.0001)))))
  }

  "ForeCastCalculator" should "return empty forecast for empty target" in {
    val request = ExchangeRequest("BTC", Array(), 1)
    //implicit val executionContext=ExecutionContext.global
    ForecastCalculator(testGETRequest).calculate(request).map(x => assert(x == ExchangeForecast("BTC", Map())))
  }

  "ForeCastCalculator" should "calculate correctly for repeated crypto" in {
    val request = ExchangeRequest("BTC", Array("BTC", "BTC"), 1)
    //implicit val executionContext=ExecutionContext.global
    ForecastCalculator(testGETRequest).calculate(request).map(x =>
      assert(x ==
        ExchangeForecast(
          "BTC",
          Map("BTC" -> SingleForecast(1, 1, 0.9999, 0.0001),
            "BTC" -> SingleForecast(1, 1, 0.9999, 0.0001)
          ))))
  }

  "ForeCastCalculator" should "calculate correctly two cryptos" in {
    val amount=5
    val request = ExchangeRequest("BTC", Array("BTC", "ETH"), amount)
    //implicit val executionContext=ExecutionContext.global
    ForecastCalculator(testGETRequest).calculate(request).map(x =>
      assert(x ==
        ExchangeForecast(
          "BTC",
          Map("BTC" -> SingleForecast(1, amount, (amount - amount * 0.0001) * 1, amount * 0.0001),
            "ETH" -> SingleForecast(14.527, amount, (amount - amount * 0.0001) * 14.527, amount * 0.0001)
          ))))
  }


}


//  val request = SimpleGETRequest("https://api.coingecko.com/api/v3/exchange_rates")
//  ForecastCalculator(request)
//    .calculate(ExchangeRequest("BTC", Array("ETH", "LTC"), 10)).onComplete(println)
//
//  def ratesData = request.responseBody().map(CoinGekoResponseStringParser().parse)
//
//  def convert = ratesData.map(x => CoinGekoRatesConversion().calculateRates("BTC", None, x))