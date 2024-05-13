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
 * @param budget 
 * @param timeBudget 
 * @param parentTitle 
 * @param customer 
 * @param id 
 * @param orderNumber 
 * @param orderDate Attention: Accessor MUST be used, otherwise date will be serialized in UTC.
 * @param start Attention: Accessor MUST be used, otherwise date will be serialized in UTC.
 * @param end Attention: Accessor MUST be used, otherwise date will be serialized in UTC.
 * @param comment 
 * @param metaFields 
 * @param teams 
 * @param budgetType 
 * @param color 
 */
@Serializable

data class ProjectEntity (

    @SerialName(value = "name") @Required val name: kotlin.String,

    @SerialName(value = "visible") @Required val visible: kotlin.Boolean,

    @SerialName(value = "billable") @Required val billable: kotlin.Boolean,

    @SerialName(value = "globalActivities") @Required val globalActivities: kotlin.Boolean,

    @SerialName(value = "budget") @Required val budget: kotlin.Float,

    @SerialName(value = "timeBudget") @Required val timeBudget: kotlin.Int,

    @SerialName(value = "parentTitle") val parentTitle: kotlin.String? = null,

    @SerialName(value = "customer") val customer: kotlin.Int? = null,

    @SerialName(value = "id") val id: kotlin.Int? = null,

    @SerialName(value = "orderNumber") val orderNumber: kotlin.String? = null,

    /* Attention: Accessor MUST be used, otherwise date will be serialized in UTC. */
    @SerialName(value = "orderDate") val orderDate: kotlin.String? = null,

    /* Attention: Accessor MUST be used, otherwise date will be serialized in UTC. */
    @SerialName(value = "start") val start: kotlin.String? = null,

    /* Attention: Accessor MUST be used, otherwise date will be serialized in UTC. */
    @SerialName(value = "end") val end: kotlin.String? = null,

    @SerialName(value = "comment") val comment: kotlin.String? = null,

    @SerialName(value = "metaFields") val metaFields: kotlin.collections.List<ProjectMeta>? = null,

    @SerialName(value = "teams") val teams: kotlin.collections.List<Team>? = null,

    @SerialName(value = "budgetType") val budgetType: kotlin.String? = null,

    @SerialName(value = "color") val color: kotlin.String? = null

)
