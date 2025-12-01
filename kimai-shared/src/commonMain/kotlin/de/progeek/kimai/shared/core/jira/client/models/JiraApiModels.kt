package de.progeek.kimai.shared.core.jira.client.models

import kotlinx.serialization.Serializable

/**
 * Internal API response models for Jira REST API v3.
 * These models match the JSON structure returned by Jira and are not exposed outside the client.
 */

@Serializable
internal data class JiraUserResponse(
    val accountId: String,
    val emailAddress: String? = null,
    val displayName: String,
    val active: Boolean
)

@Serializable
internal data class JiraProjectResponse(
    val id: String,
    val key: String,
    val name: String,
    val description: String? = null,
    val projectTypeKey: String? = null
)

@Serializable
internal data class JiraIssueResponse(
    val id: String,
    val key: String,
    val fields: JiraIssueFields
)

@Serializable
internal data class JiraIssueFields(
    val summary: String,
    val description: String? = null,
    val status: JiraStatus? = null,
    val assignee: JiraUserResponse? = null,
    val project: JiraProjectShort? = null,
    val issuetype: JiraIssueType? = null,
    val updated: String? = null
)

@Serializable
internal data class JiraStatus(
    val name: String
)

@Serializable
internal data class JiraProjectShort(
    val key: String,
    val name: String
)

@Serializable
internal data class JiraIssueType(
    val name: String
)

@Serializable
internal data class JiraSearchResponse(
    val issues: List<JiraIssueResponse> = emptyList(),
    val startAt: Int = 0,
    val maxResults: Int = 0,
    val total: Int = 0
)
