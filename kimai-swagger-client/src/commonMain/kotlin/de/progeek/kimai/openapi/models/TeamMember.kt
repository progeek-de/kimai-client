/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package de.progeek.kimai.openapi.models

import kotlinx.serialization.*

/**
 * 
 *
 * @param user 
 * @param teamlead 
 */
@Serializable

data class TeamMember (

    @SerialName(value = "user") @Required val user: User,

    @SerialName(value = "teamlead") @Required val teamlead: kotlin.Boolean

)

