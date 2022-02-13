package org.pawelsadlo2
package calculations

import calculations.CoinGekoRatesConversion.Quotation

import org.pawelsadlo2.parsing.coinGeko.CoinGekoResponseStringParser.CoinGekoRatesFormat

object CoinGekoRatesConversion {

  def apply() = new CoinGekoRatesConversion

  final case class Quotation(source: String, rates: Map[String, BigDecimal])

}

class CoinGekoRatesConversion extends RatesConversion {

  def calculateRates[CC <: Iterable[CoinGekoRatesFormat], DD <: Iterable[String]](source: String, targets: Option[DD], data: CC) = {
    //RATE IN API IS AGAINST BTC
    //println(source,targets.toString,data.toString)
    val toBtcRate = (cryptoName: String) => data.find(x => x.name.toUpperCase == cryptoName.toUpperCase)
    val sourceToBtcRate = toBtcRate(source)

    def mapFormat(rates: CoinGekoRatesFormat) = rates.name.toUpperCase -> rates.data.value

    sourceToBtcRate match {
      case Some(rate) =>
        targets match {
          case Some(xs) => Quotation(source, data.withFilter(x => xs.toList.contains(x.name.toUpperCase)).map(mapFormat).toMap.view.mapValues(_ / rate.data.value).toMap)
          case None => Quotation(source, data.map(mapFormat).toMap.view.mapValues(_ / rate.data.value).toMap)
        }
      case None => Quotation(source, Map.empty)
    }
  }


}

