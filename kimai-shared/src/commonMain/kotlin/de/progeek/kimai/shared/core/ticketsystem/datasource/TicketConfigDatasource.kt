@file:OptIn(kotlin.time.ExperimentalTime::class)

package de.progeek.kimai.shared.core.ticketsystem.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher
import de.progeek.kimai.shared.core.ticketsystem.models.TicketCredentials
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Datasource for ticket system configuration storage.
 * Handles encryption of credentials.
 */
class TicketConfigDatasource(
    private val database: KimaiDatabase,
    private val aesCipher: AesGCMCipher
) {
    private val query get() = database.ticketConfigEntityQueries
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Get all configurations as a Flow.
     */
    fun getAll(): Flow<List<TicketSystemConfig>> =
        query.getAll()
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { entities -> entities.mapNotNull { it.toConfig() } }

    /**
     * Get only enabled configurations.
     */
    fun getEnabled(): Flow<List<TicketSystemConfig>> =
        query.getEnabled()
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { entities -> entities.mapNotNull { it.toConfig() } }

    /**
     * Get a specific configuration by ID.
     */
    fun getById(id: String): Flow<TicketSystemConfig?> =
        query.getById(id)
            .asFlow()
            .mapToOneOrNull(kimaiDispatchers.io)
            .map { it?.toConfig() }

    /**
     * Get configurations for a specific provider.
     */
    fun getByProvider(provider: TicketProvider): Flow<List<TicketSystemConfig>> =
        query.getByProvider(provider.name)
            .asFlow()
            .mapToList(kimaiDispatchers.io)
            .map { entities -> entities.mapNotNull { it.toConfig() } }

    /**
     * Save a configuration (insert or update).
     */
    suspend fun save(config: TicketSystemConfig): Result<TicketSystemConfig> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                val encryptedCredentials = encryptCredentials(config.credentials)
                query.insert(
                    id = config.id,
                    displayName = config.displayName,
                    provider = config.provider.name,
                    enabled = if (config.enabled) 1L else 0L,
                    baseUrl = config.baseUrl,
                    credentialsJson = encryptedCredentials,
                    syncIntervalMinutes = config.syncIntervalMinutes.toLong(),
                    defaultProjectKey = config.defaultProjectKey,
                    createdAt = config.createdAt.toEpochMilliseconds(),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    issueFormat = config.issueFormat
                )
                config
            }
        }

    /**
     * Delete a configuration.
     */
    suspend fun delete(id: String): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.delete(id)
                Unit
            }
        }

    /**
     * Toggle enabled state for a configuration.
     */
    suspend fun setEnabled(id: String, enabled: Boolean): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.setEnabled(
                    enabled = if (enabled) 1L else 0L,
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    id = id
                )
                Unit
            }
        }

    /**
     * Update sync interval for a configuration.
     */
    suspend fun updateSyncInterval(id: String, intervalMinutes: Int): Result<Unit> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.updateSyncInterval(
                    syncIntervalMinutes = intervalMinutes.toLong(),
                    updatedAt = Clock.System.now().toEpochMilliseconds(),
                    id = id
                )
                Unit
            }
        }

    /**
     * Get total count of configurations.
     */
    suspend fun count(): Result<Long> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.count().executeAsOne()
            }
        }

    /**
     * Get count of enabled configurations.
     */
    suspend fun countEnabled(): Result<Long> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.countEnabled().executeAsOne()
            }
        }

    /**
     * Check if there are any enabled configurations.
     */
    fun hasEnabled(): Flow<Boolean> =
        getEnabled().map { it.isNotEmpty() }

    private fun encryptCredentials(credentials: TicketCredentials): String {
        val plainJson = json.encodeToString(credentials)
        return aesCipher.encryptString(plainJson)
    }

    private fun decryptCredentials(encrypted: String): TicketCredentials? {
        return try {
            val plainJson = aesCipher.decryptString(encrypted) ?: return null
            json.decodeFromString<TicketCredentials>(plainJson)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Extension function to convert database entity to domain model.
     */
    private fun de.progeek.kimai.shared.TicketConfigEntity.toConfig(): TicketSystemConfig? {
        val decryptedCredentials = decryptCredentials(credentialsJson) ?: return null
        val providerType = TicketProvider.fromString(provider) ?: return null

        return TicketSystemConfig(
            id = id,
            displayName = displayName,
            provider = providerType,
            enabled = enabled == 1L,
            baseUrl = baseUrl,
            credentials = decryptedCredentials,
            syncIntervalMinutes = syncIntervalMinutes.toInt(),
            defaultProjectKey = defaultProjectKey,
            createdAt = Instant.fromEpochMilliseconds(createdAt),
            updatedAt = Instant.fromEpochMilliseconds(updatedAt),
            issueFormat = issueFormat
        )
    }
}
