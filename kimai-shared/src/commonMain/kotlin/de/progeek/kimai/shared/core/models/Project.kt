package de.progeek.kimai.shared.core.models

data class Project(
    val id: Long,
    val name: String,
    val parent: String,
    val globalActivities: Boolean,
    val customer: Customer?
)