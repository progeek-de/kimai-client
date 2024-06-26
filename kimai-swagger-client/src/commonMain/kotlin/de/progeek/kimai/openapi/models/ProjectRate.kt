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
 * @param isFixed 
 * @param id 
 * @param user 
 * @param rate 
 * @param internalRate 
 */
@Serializable

data class ProjectRate (

    @SerialName(value = "isFixed") @Required val isFixed: kotlin.Boolean,

    @SerialName(value = "id") val id: kotlin.Int? = null,

    @SerialName(value = "user") val user: User? = null,

    @SerialName(value = "rate") val rate: kotlin.Float? = null,

    @SerialName(value = "internalRate") val internalRate: kotlin.Float? = null

)

