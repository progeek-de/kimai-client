package de.progeek.kimai.shared.core.ticketsystem.models

/**
 * Unified project model for all ticket system providers.
 */
data class TicketProject(
    val key: String,
    val name: String,
    val description: String? = null
)
