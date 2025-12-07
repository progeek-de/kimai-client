package de.progeek.kimai.shared.core.ticketsystem.providers.github

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Internal API response models for GitHub REST API v3.
 */

@Serializable
internal data class GitHubUserResponse(
    val id: Long,
    val login: String,
    val name: String? = null,
    val email: String? = null
)

@Serializable
internal data class GitHubRepoResponse(
    val id: Long,
    val name: String,
    @SerialName("full_name")
    val fullName: String,
    val description: String? = null,
    val owner: GitHubOwner
)

@Serializable
internal data class GitHubOwner(
    val login: String
)

@Serializable
internal data class GitHubIssueResponse(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String? = null,
    val state: String,
    val user: GitHubUserResponse? = null,
    val assignee: GitHubUserResponse? = null,
    val labels: List<GitHubLabel> = emptyList(),
    @SerialName("html_url")
    val htmlUrl: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("repository_url")
    val repositoryUrl: String? = null
)

@Serializable
internal data class GitHubLabel(
    val name: String,
    val color: String? = null
)

@Serializable
internal data class GitHubSearchResponse(
    @SerialName("total_count")
    val totalCount: Int,
    @SerialName("incomplete_results")
    val incompleteResults: Boolean,
    val items: List<GitHubIssueResponse>
)
