package org.pawelsadlo2
package parsing

trait ResponseStringParser[T, CC <: Iterable[T]] {
  def parse(response: String): CC
}
