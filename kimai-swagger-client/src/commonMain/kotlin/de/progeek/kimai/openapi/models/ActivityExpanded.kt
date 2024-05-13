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
 * @param id 
 * @param project 
 * @param comment 
 * @param color 
 */
@Serializable

data class ActivityExpanded (

    @SerialName(value = "name") @Required val name: kotlin.String,

    @SerialName(value = "visible") @Required val visible: kotlin.Boolean,

    @SerialName(value = "billable") @Required val billable: kotlin.Boolean,

    @SerialName(value = "id") val id: kotlin.Int? = null,

    @SerialName(value = "project") val project: ProjectExpanded? = null,

    @SerialName(value = "comment") val comment: kotlin.String? = null,

    @SerialName(value = "color") val color: kotlin.String? = null

)
