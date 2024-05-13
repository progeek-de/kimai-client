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
 * @param visible 
 * @param billable 
 * @param globalActivities 
 * @param customer 
 * @param id 
 * @param comment 
 * @param color 
 */
@Serializable

data class Project (

    @SerialName(value = "name") @Required val name: kotlin.String,

    @SerialName(value = "visible") @Required val visible: kotlin.Boolean,

    @SerialName(value = "billable") @Required val billable: kotlin.Boolean,

    @SerialName(value = "globalActivities") @Required val globalActivities: kotlin.Boolean,

    @SerialName(value = "customer") val customer: kotlin.Int? = null,

    @SerialName(value = "id") val id: kotlin.Int? = null,

    @SerialName(value = "comment") val comment: kotlin.String? = null,

    @SerialName(value = "color") val color: kotlin.String? = null

)
