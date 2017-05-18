package com.servicediscovery.exceptions

import akka.actor.ActorRef
import com.servicediscovery.models.{Connection, Location}

/**
 * An Exception representing failure in registration
 *
 * @param connection A connection for which registration failed
 */
case class RegistrationFailed(connection: Connection)
    extends RuntimeException(
      s"unable to register $connection"
    )

/**
 * An Exception representing failure in un-registration
 *
 * @param connection A connection for which un-registration failed
 */
case class UnregistrationFailed(connection: Connection)
    extends RuntimeException(
      s"unable to unregister $connection"
    )

/**
 * An Exception representing failure in registration as other location is already registered in place of the given location
 *
 * @param location      The location which is supposed to be registered
 * @param otherLocation The location which already registered
 */
case class OtherLocationIsRegistered(location: Location, otherLocation: Location)
    extends RuntimeException(
      s"there is other location=$otherLocation registered against name=${location.connection.name}."
    )

/**
 * An Exception representing failure in listing locations
 */
case object RegistrationListingFailed
    extends RuntimeException(
      s"unable to get the list of registered locations"
    )

/**
 * An Exception representing failure in registering non remote actors
 */
case class LocalAkkaActorRegistrationNotAllowed(actorRef: ActorRef)
    extends RuntimeException(
      s"Registration of only remote actors is allowed. Instead local actor $actorRef received."
    )
