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
 * @param user User ID
 * @param teamlead Whether the user is a teamlead
 */
@Serializable

data class TeamEditFormMembersInner (

    /* User ID */
    @SerialName(value = "user") val user: kotlin.Int? = null,

    /* Whether the user is a teamlead */
    @SerialName(value = "teamlead") val teamlead: kotlin.Boolean? = null

)

