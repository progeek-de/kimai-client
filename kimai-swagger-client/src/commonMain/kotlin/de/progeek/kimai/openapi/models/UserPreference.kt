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
 * @param name 
 * @param `value` 
 */
@Serializable

data class UserPreference (

    @SerialName(value = "name") @Required val name: kotlin.String,

    @SerialName(value = "value") val `value`: kotlin.String? = null

)
