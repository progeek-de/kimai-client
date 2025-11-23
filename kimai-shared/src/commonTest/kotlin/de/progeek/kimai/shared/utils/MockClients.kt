package de.progeek.kimai.shared.utils

import de.progeek.kimai.shared.core.network.client.ActivityClient
import de.progeek.kimai.shared.core.network.client.AuthClient
import de.progeek.kimai.shared.core.network.client.CustomerClient
import de.progeek.kimai.shared.core.network.client.ProjectClient
import de.progeek.kimai.shared.core.network.client.TimesheetsClient
import io.mockk.mockk

/**
 * Mock network client factory functions for testing.
 * These functions create MockK instances with configurable behavior.
 */

/**
 * Creates a mock TimesheetsClient.
 */
fun mockTimesheetsClient(): TimesheetsClient = mockk(relaxed = true)

/**
 * Creates a mock ProjectClient.
 */
fun mockProjectClient(): ProjectClient = mockk(relaxed = true)

/**
 * Creates a mock ActivityClient.
 */
fun mockActivityClient(): ActivityClient = mockk(relaxed = true)

/**
 * Creates a mock CustomerClient.
 */
fun mockCustomerClient(): CustomerClient = mockk(relaxed = true)

/**
 * Creates a mock AuthClient.
 */
fun mockAuthClient(): AuthClient = mockk(relaxed = true)
