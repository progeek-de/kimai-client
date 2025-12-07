package de.progeek.kimai.shared.core.ticketsystem.repository

import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketConfigDatasource
import de.progeek.kimai.shared.core.ticketsystem.datasource.TicketIssueDatasource
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing ticket system configurations.
 * Handles CRUD operations and provides reactive access to configurations.
 */
class TicketConfigRepository(
    private val configDatasource: TicketConfigDatasource,
    private val issueDatasource: TicketIssueDatasource
) {

    /**
     * Get all configurations.
     */
    fun getAllConfigs(): Flow<List<TicketSystemConfig>> = configDatasource.getAll()

    /**
     * Get only enabled configurations.
     */
    fun getEnabledConfigs(): Flow<List<TicketSystemConfig>> = configDatasource.getEnabled()

    /**
     * Get a specific configuration by ID.
     */
    fun getConfigById(id: String): Flow<TicketSystemConfig?> = configDatasource.getById(id)

    /**
     * Get configurations for a specific provider type.
     */
    fun getConfigsByProvider(provider: TicketProvider): Flow<List<TicketSystemConfig>> =
        configDatasource.getByProvider(provider)

    /**
     * Save a configuration (create or update).
     */
    suspend fun saveConfig(config: TicketSystemConfig): Result<TicketSystemConfig> =
        configDatasource.save(config)

    /**
     * Delete a configuration and its cached issues.
     */
    suspend fun deleteConfig(id: String): Result<Unit> {
        // First delete cached issues for this source
        issueDatasource.deleteBySource(id)
        // Then delete the configuration
        return configDatasource.delete(id)
    }

    /**
     * Enable or disable a configuration.
     */
    suspend fun setEnabled(id: String, enabled: Boolean): Result<Unit> =
        configDatasource.setEnabled(id, enabled)

    /**
     * Update sync interval for a configuration.
     */
    suspend fun updateSyncInterval(id: String, intervalMinutes: Int): Result<Unit> =
        configDatasource.updateSyncInterval(id, intervalMinutes)

    /**
     * Get total count of configurations.
     */
    suspend fun count(): Result<Long> = configDatasource.count()

    /**
     * Get count of enabled configurations.
     */
    suspend fun countEnabled(): Result<Long> = configDatasource.countEnabled()

    /**
     * Check if there are any enabled configurations.
     */
    fun hasEnabledConfigs(): Flow<Boolean> = configDatasource.hasEnabled()
}
