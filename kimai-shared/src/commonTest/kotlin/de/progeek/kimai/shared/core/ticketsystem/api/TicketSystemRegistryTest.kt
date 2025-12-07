package de.progeek.kimai.shared.core.ticketsystem.api

import de.progeek.kimai.shared.core.ticketsystem.models.TicketIssue
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProject
import de.progeek.kimai.shared.core.ticketsystem.models.TicketProvider
import de.progeek.kimai.shared.core.ticketsystem.models.TicketSystemConfig
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for TicketSystemRegistry.
 *
 * Tests the provider registration and lookup functionality.
 */
class TicketSystemRegistryTest {

    private lateinit var registry: TicketSystemRegistry

    @BeforeTest
    fun setup() {
        registry = TicketSystemRegistry()
    }

    // ============================================================
    // register() Tests
    // ============================================================

    @Test
    fun `register adds provider to registry`() {
        val provider = createMockProvider(TicketProvider.JIRA)

        registry.register(provider)

        assertTrue(registry.isSupported(TicketProvider.JIRA))
    }

    @Test
    fun `register multiple providers works correctly`() {
        registry.register(createMockProvider(TicketProvider.JIRA))
        registry.register(createMockProvider(TicketProvider.GITHUB))
        registry.register(createMockProvider(TicketProvider.GITLAB))

        assertTrue(registry.isSupported(TicketProvider.JIRA))
        assertTrue(registry.isSupported(TicketProvider.GITHUB))
        assertTrue(registry.isSupported(TicketProvider.GITLAB))
    }

    @Test
    fun `register replaces existing provider of same type`() {
        val provider1 = createMockProvider(TicketProvider.JIRA)
        val provider2 = createMockProvider(TicketProvider.JIRA)

        registry.register(provider1)
        registry.register(provider2)

        val retrieved = registry.getProvider(TicketProvider.JIRA)
        assertEquals(provider2, retrieved)
    }

    // ============================================================
    // getProvider() Tests
    // ============================================================

    @Test
    fun `getProvider returns registered provider`() {
        val provider = createMockProvider(TicketProvider.GITHUB)
        registry.register(provider)

        val result = registry.getProvider(TicketProvider.GITHUB)

        assertNotNull(result)
        assertEquals(TicketProvider.GITHUB, result.providerType)
    }

    @Test
    fun `getProvider returns null for unregistered provider`() {
        val result = registry.getProvider(TicketProvider.JIRA)

        assertNull(result)
    }

    @Test
    fun `getProvider returns correct provider among multiple`() {
        val jiraProvider = createMockProvider(TicketProvider.JIRA)
        val githubProvider = createMockProvider(TicketProvider.GITHUB)

        registry.register(jiraProvider)
        registry.register(githubProvider)

        val result = registry.getProvider(TicketProvider.GITHUB)

        assertEquals(githubProvider, result)
    }

    // ============================================================
    // getAllProviders() Tests
    // ============================================================

    @Test
    fun `getAllProviders returns empty list when no providers registered`() {
        val result = registry.getAllProviders()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getAllProviders returns all registered providers`() {
        registry.register(createMockProvider(TicketProvider.JIRA))
        registry.register(createMockProvider(TicketProvider.GITHUB))
        registry.register(createMockProvider(TicketProvider.GITLAB))

        val result = registry.getAllProviders()

        assertEquals(3, result.size)
    }

    @Test
    fun `getAllProviders returns providers with correct types`() {
        registry.register(createMockProvider(TicketProvider.JIRA))
        registry.register(createMockProvider(TicketProvider.GITHUB))

        val result = registry.getAllProviders()
        val types = result.map { it.providerType }

        assertTrue(types.contains(TicketProvider.JIRA))
        assertTrue(types.contains(TicketProvider.GITHUB))
    }

    // ============================================================
    // getSupportedProviders() Tests
    // ============================================================

    @Test
    fun `getSupportedProviders returns empty list when no providers registered`() {
        val result = registry.getSupportedProviders()

        assertTrue(result.isEmpty())
    }

    @Test
    fun `getSupportedProviders returns all registered provider types`() {
        registry.register(createMockProvider(TicketProvider.JIRA))
        registry.register(createMockProvider(TicketProvider.GITLAB))

        val result = registry.getSupportedProviders()

        assertEquals(2, result.size)
        assertTrue(result.contains(TicketProvider.JIRA))
        assertTrue(result.contains(TicketProvider.GITLAB))
    }

    // ============================================================
    // isSupported() Tests
    // ============================================================

    @Test
    fun `isSupported returns false when no providers registered`() {
        assertFalse(registry.isSupported(TicketProvider.JIRA))
    }

    @Test
    fun `isSupported returns true for registered provider`() {
        registry.register(createMockProvider(TicketProvider.JIRA))

        assertTrue(registry.isSupported(TicketProvider.JIRA))
    }

    @Test
    fun `isSupported returns false for unregistered provider`() {
        registry.register(createMockProvider(TicketProvider.JIRA))

        assertFalse(registry.isSupported(TicketProvider.GITHUB))
    }

    @Test
    fun `isSupported works correctly with all provider types`() {
        registry.register(createMockProvider(TicketProvider.GITHUB))

        assertFalse(registry.isSupported(TicketProvider.JIRA))
        assertTrue(registry.isSupported(TicketProvider.GITHUB))
        assertFalse(registry.isSupported(TicketProvider.GITLAB))
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun createMockProvider(type: TicketProvider): TicketSystemProvider {
        return object : TicketSystemProvider {
            override val providerType: TicketProvider = type

            override suspend fun testConnection(config: TicketSystemConfig): Result<String> {
                return Result.success("Test User")
            }

            override suspend fun searchIssues(
                config: TicketSystemConfig,
                query: String,
                maxResults: Int
            ): Result<List<TicketIssue>> {
                return Result.success(emptyList())
            }

            override suspend fun getIssueByKey(
                config: TicketSystemConfig,
                key: String
            ): Result<TicketIssue?> {
                return Result.success(null)
            }

            override suspend fun getProjects(config: TicketSystemConfig): Result<List<TicketProject>> {
                return Result.success(emptyList())
            }

            override suspend fun getCurrentUser(config: TicketSystemConfig): Result<String> {
                return Result.success("test-user")
            }

            override fun validateCredentials(config: TicketSystemConfig): Boolean {
                return true
            }

            override fun getErrorMessage(exception: Throwable, config: TicketSystemConfig): String {
                return exception.message ?: "Unknown error"
            }
        }
    }
}
