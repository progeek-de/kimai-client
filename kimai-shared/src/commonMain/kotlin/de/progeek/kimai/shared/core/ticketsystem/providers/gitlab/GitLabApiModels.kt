package de.progeek.kimai.shared.core.ticketsystem.providers.gitlab

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal API response models for GitLab REST API v4.
 */

@Serializable
internal data class GitLabUserResponse(
    val id: Long,
    val username: String,
    val name: String,
    val email: String? = null,
    @SerialName("web_url")
    val webUrl: String? = null
)

@Serializable
internal data class GitLabProjectResponse(
    val id: Long,
    val name: String,
    @SerialName("path_with_namespace")
    val pathWithNamespace: String,
    val description: String? = null,
    @SerialName("web_url")
    val webUrl: String
)

@Serializable
internal data class GitLabIssueResponse(
    val id: Long,
    val iid: Int,
    val title: String,
    val description: String? = null,
    val state: String,
    val author: GitLabUserResponse? = null,
    val assignee: GitLabUserResponse? = null,
    val assignees: List<GitLabUserResponse> = emptyList(),
    val labels: List<String> = emptyList(),
    @SerialName("web_url")
    val webUrl: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("project_id")
    val projectId: Long,
    val references: GitLabReferences? = null
)

@Serializable
internal data class GitLabReferences(
    val short: String? = null,
    val relative: String? = null,
    val full: String? = null
)
