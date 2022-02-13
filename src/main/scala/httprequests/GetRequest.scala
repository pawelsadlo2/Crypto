package org.pawelsadlo2
package httprequests

import scala.concurrent.Future

trait GetRequest {
  def responseBody():Future[String]
}
