package de.progeek.kimai.shared.core.ticketsystem.api

import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider

/**
 * Registry for all available ticket system provider implementations.
 * Used to look up the correct provider implementation for a given configuration.
 */
class TicketSystemRegistry {
    private val providers = mutableMapOf<TicketProvider, TicketSystemProvider>()

    /**
     * Register a provider implementation.
     *
     * @param provider The provider implementation to register.
     */
    fun register(provider: TicketSystemProvider) {
        providers[provider.providerType] = provider
    }

    /**
     * Get the provider implementation for a given type.
     *
     * @param type The provider type to look up.
     * @return The provider implementation, or null if not registered.
     */
    fun getProvider(type: TicketProvider): TicketSystemProvider? = providers[type]

    /**
     * Get all registered providers.
     *
     * @return List of all registered provider implementations.
     */
    fun getAllProviders(): List<TicketSystemProvider> = providers.values.toList()

    /**
     * Get all supported provider types.
     *
     * @return List of all registered provider types.
     */
    fun getSupportedProviders(): List<TicketProvider> = providers.keys.toList()

    /**
     * Check if a provider type is supported.
     *
     * @param type The provider type to check.
     * @return true if the provider is registered, false otherwise.
     */
    fun isSupported(type: TicketProvider): Boolean = providers.containsKey(type)
}
