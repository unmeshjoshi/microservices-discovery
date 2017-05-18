package com.servicediscovery.models

/**
 * Marker trait for serializing domain models over the wire.
 * This marker is configured to be serialized using Kryo.
 */
trait TmtSerializable extends Serializable
