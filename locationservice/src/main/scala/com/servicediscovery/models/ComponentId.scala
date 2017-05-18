package com.servicediscovery.models

/**
 * Represents a component based on its name and type.
 *
 * ''Note : '' Name should not contain
 *  - leading or trailing spaces
 *  - and hyphen (-)
 */
case class ComponentId(name: String, componentType: ComponentType) extends TmtSerializable {

  require(name == name.trim, "component name has leading and trailing whitespaces")

  require(!name.contains("-"), "component name has '-'")
}
