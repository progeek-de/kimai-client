package de.progeek.kimai.shared.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Activity(val id: Long, val name: String, val project: Long?)
