package org.pawelsadlo2
package parsing.coinGeko

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser.{CoinGekoRatesFormat, CoinGekoDataEntry, jsonFormat2, jsonFormat4}
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

trait CoinGekoJsonSupport extends SprayJsonSupport with DefaultJsonProtocol{
  implicit val dataFormat: RootJsonFormat[CoinGekoDataEntry] = jsonFormat4(CoinGekoDataEntry)
  implicit val cryptoEntryFormat: RootJsonFormat[CoinGekoRatesFormat] = jsonFormat2(CoinGekoRatesFormat)
}
