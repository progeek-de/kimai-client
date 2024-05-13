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
 * @param username 
 * @param initials 
 * @param id 
 * @param alias 
 * @param title 
 * @param accountNumber 
 * @param enabled 
 * @param color 
 */
@Serializable

data class UserCollection (

    @SerialName(value = "username") @Required val username: kotlin.String,

    @SerialName(value = "initials") val initials: kotlin.String? = null,

    @SerialName(value = "id") val id: kotlin.Int? = null,

    @SerialName(value = "alias") val alias: kotlin.String? = null,

    @SerialName(value = "title") val title: kotlin.String? = null,

    @SerialName(value = "accountNumber") val accountNumber: kotlin.String? = null,

    @SerialName(value = "enabled") val enabled: kotlin.Boolean? = null,

    @SerialName(value = "color") val color: kotlin.String? = null

)
