package de.progeek.kimai.shared.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Customer(val id: Long, val name: String)
