package org.pawelsadlo2
package calculations

import parsing.coinGeko.CoinGekoResponseStringParser.CoinGekoRatesFormat

import org.pawelsadlo2.calculations.CoinGekoRatesConversion.Quotation

trait RatesConversion {
  def calculateRates[IterableSubclass <: Iterable[CoinGekoRatesFormat],U<:Iterable[String]](source: String, targets: Option[U], data: IterableSubclass):Quotation
}
