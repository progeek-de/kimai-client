package de.progeek.kimai.shared.core.ticketsystem.models

/**
 * Supported ticket system providers.
 */
enum class TicketProvider(val displayName: String) {
    JIRA("Jira"),
    GITHUB("GitHub Issues"),
    GITLAB("GitLab Issues");

    companion object {
        fun fromString(value: String): TicketProvider? =
            entries.find { it.name.equals(value, ignoreCase = true) }
    }
}
