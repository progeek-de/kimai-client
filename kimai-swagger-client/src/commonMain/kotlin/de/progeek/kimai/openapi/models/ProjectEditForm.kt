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
 * @param customer Customer ID
 * @param comment 
 * @param invoiceText 
 * @param orderNumber 
 * @param orderDate 
 * @param start 
 * @param end 
 * @param color The hexadecimal color code (default: #d2d6de)
 * @param globalActivities 
 * @param visible 
 * @param billable 
 */
@Serializable

data class ProjectEditForm (

    @SerialName(value = "name") @Required val name: kotlin.String,

    /* Customer ID */
    @SerialName(value = "customer") @Required val customer: kotlin.Int,

    @SerialName(value = "comment") val comment: kotlin.String? = null,

    @SerialName(value = "invoiceText") val invoiceText: kotlin.String? = null,

    @SerialName(value = "orderNumber") val orderNumber: kotlin.String? = null,

    @SerialName(value = "orderDate") val orderDate: kotlin.String? = null,

    @SerialName(value = "start") val start: kotlin.String? = null,

    @SerialName(value = "end") val end: kotlin.String? = null,

    /* The hexadecimal color code (default: #d2d6de) */
    @SerialName(value = "color") val color: kotlin.String? = null,

    @SerialName(value = "globalActivities") val globalActivities: kotlin.Boolean? = null,

    @SerialName(value = "visible") val visible: kotlin.Boolean? = null,

    @SerialName(value = "billable") val billable: kotlin.Boolean? = null

)
