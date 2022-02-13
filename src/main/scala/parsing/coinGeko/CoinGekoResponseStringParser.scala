package org.pawelsadlo2
package parsing.coinGeko

import parsing.ResponseStringParser

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser.{firstKey,CoinGekoDataEntry,CoinGekoRatesFormat}
import spray.json._

import scala.collection.immutable
import scala.util.{Failure, Success, Try}


object CoinGekoResponseStringParser extends SprayJsonSupport with DefaultJsonProtocol{
  def apply(): CoinGekoResponseStringParser = new CoinGekoResponseStringParser

  final val firstKey = "rates"


  final case class CoinGekoRatesFormat(name: String, data: CoinGekoDataEntry)
  final case class CoinGekoDataEntry(name: String, `type`: String, unit: String, value: BigDecimal)

}

class CoinGekoResponseStringParser extends ResponseStringParser[CoinGekoRatesFormat, Iterable[CoinGekoRatesFormat]] with CoinGekoJsonSupport {

  def parse(response: String): immutable.Iterable[CoinGekoRatesFormat] = Try(response.parseJson) match {
    case Success(x) =>
      x.asJsObject.fields.getOrElse(firstKey, throw new NoSuchElementException("response parsing failed - wrong structure"))
        .asJsObject.fields.map { x => CoinGekoRatesFormat(x._1, x._2.convertTo[CoinGekoDataEntry]) }
    case Failure(ex) => ???
  }

}

