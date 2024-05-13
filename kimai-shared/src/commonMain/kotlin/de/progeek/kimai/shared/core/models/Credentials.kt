package de.progeek.kimai.shared.core.models

import kotlinx.serialization.Serializable

@Serializable
data class Credentials(val email: String, val password: String)
