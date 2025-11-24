package de.progeek.kimai.shared.core.jira.models

/**
 * Domain model representing a Jira project.
 *
 * @property key Project key (e.g., "PROJ")
 * @property name Project name
 * @property description Project description (nullable)
 */
data class JiraProject(
    val key: String,
    val name: String,
    val description: String?
)
