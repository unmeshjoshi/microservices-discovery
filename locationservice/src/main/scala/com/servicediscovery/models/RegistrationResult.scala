package com.servicediscovery.models

import akka.Done

import scala.concurrent.Future

/**
 * RegistrationResult represents successful registration of a location.
 */
trait RegistrationResult {

  /**
   * The successful registration of location can be unregistered using this method
   */
  def unregister(): Future[Done]

  def location: Location
}
