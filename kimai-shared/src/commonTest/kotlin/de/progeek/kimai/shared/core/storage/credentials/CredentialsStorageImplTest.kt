package de.progeek.kimai.shared.core.storage.credentials

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.shared.core.models.Credentials
import io.mockk.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test suite for CredentialsStorageImpl (CredentialsRepository implementation).
 *
 * Tests the following methods:
 * 1. save(cred) - Encrypts and saves credentials
 * 2. get() - Returns Flow<Credentials?> of decrypted credentials
 * 3. getCredentials() - Returns Credentials? synchronously
 * 4. delete() - Removes stored credentials
 *
 * Tests verify encryption/decryption integration and settings storage.
 */
class CredentialsStorageImplTest {

    private lateinit var mockSettings: ObservableSettings
    private lateinit var mockCipher: AesGCMCipher
    private lateinit var repository: CredentialsStorageImpl

    private val testCredentials = Credentials("test@example.com", "password123")
    private val encryptedData = "encrypted_data_string"

    @BeforeTest
    fun setup() {
        mockSettings = mockk(relaxed = true)
        mockCipher = mockk(relaxed = true)
        @OptIn(com.russhwolf.settings.ExperimentalSettingsApi::class)
        repository = CredentialsStorageImpl(mockSettings, mockCipher)
    }

    @AfterTest
    fun teardown() {
        clearAllMocks()
    }

    // ============================================================
    // save() Tests
    // ============================================================

    @Test
    fun `save encrypts and stores credentials successfully`() = runTest {
        // Given
        every { mockCipher.encrypt(testCredentials) } returns encryptedData
        every { mockSettings.putString(CredentialsConstants.CREDENTIALS_KEY, encryptedData) } just Runs

        // When
        val result = repository.save(testCredentials)

        // Then
        assertTrue(result.isSuccess)
        verify { mockCipher.encrypt(testCredentials) }
        verify { mockSettings.putString(CredentialsConstants.CREDENTIALS_KEY, encryptedData) }
    }

    @Test
    fun `save handles encryption with different credentials`() = runTest {
        // Given
        val differentCreds = Credentials("other@example.com", "different_pass")
        val differentEncrypted = "different_encrypted_data"
        every { mockCipher.encrypt(differentCreds) } returns differentEncrypted
        every { mockSettings.putString(any(), any()) } just Runs

        // When
        val result = repository.save(differentCreds)

        // Then
        assertTrue(result.isSuccess)
        verify { mockCipher.encrypt(differentCreds) }
        verify { mockSettings.putString(CredentialsConstants.CREDENTIALS_KEY, differentEncrypted) }
    }

    @Test
    fun `save can be called multiple times`() = runTest {
        // Given
        every { mockCipher.encrypt(any()) } returns encryptedData
        every { mockSettings.putString(any(), any()) } just Runs

        // When
        val result1 = repository.save(testCredentials)
        val result2 = repository.save(testCredentials)

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        verify(exactly = 2) { mockCipher.encrypt(testCredentials) }
        verify(exactly = 2) { mockSettings.putString(any(), any()) }
    }

    // ============================================================
    // getCredentials() Tests
    // ============================================================

    @Test
    fun `getCredentials returns decrypted credentials when stored`() = runTest {
        // Given
        every { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) } returns encryptedData
        every { mockCipher.decrypt(encryptedData) } returns testCredentials

        // When
        val result = repository.getCredentials()

        // Then
        assertNotNull(result)
        assertEquals(testCredentials.email, result.email)
        assertEquals(testCredentials.password, result.password)
        verify { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) }
        verify { mockCipher.decrypt(encryptedData) }
    }

    @Test
    fun `getCredentials returns null when no credentials stored`() = runTest {
        // Given
        every { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) } returns null
        every { mockCipher.decrypt(null) } returns null

        // When
        val result = repository.getCredentials()

        // Then
        assertNull(result)
        verify { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) }
    }

    @Test
    fun `getCredentials returns null when decryption fails`() = runTest {
        // Given
        every { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) } returns encryptedData
        every { mockCipher.decrypt(encryptedData) } returns null

        // When
        val result = repository.getCredentials()

        // Then
        assertNull(result)
    }

    // ============================================================
    // delete() Tests
    // ============================================================

    @Test
    fun `delete removes credentials from settings`() = runTest {
        // Given
        every { mockSettings.remove(CredentialsConstants.CREDENTIALS_KEY) } just Runs

        // When
        val result = repository.delete()

        // Then
        assertTrue(result.isSuccess)
        verify { mockSettings.remove(CredentialsConstants.CREDENTIALS_KEY) }
    }

    @Test
    fun `delete can be called multiple times`() = runTest {
        // Given
        every { mockSettings.remove(any()) } just Runs

        // When
        val result1 = repository.delete()
        val result2 = repository.delete()

        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        verify(exactly = 2) { mockSettings.remove(CredentialsConstants.CREDENTIALS_KEY) }
    }

    @Test
    fun `delete succeeds even when no credentials stored`() = runTest {
        // Given - settings.remove doesn't fail if key doesn't exist
        every { mockSettings.remove(any()) } just Runs

        // When
        val result = repository.delete()

        // Then
        assertTrue(result.isSuccess)
    }

    // ============================================================
    // Integration Tests
    // ============================================================

    @Test
    fun `save and getCredentials flow works correctly`() = runTest {
        // Given
        every { mockCipher.encrypt(testCredentials) } returns encryptedData
        every { mockSettings.putString(any(), any()) } just Runs
        every { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) } returns encryptedData
        every { mockCipher.decrypt(encryptedData) } returns testCredentials

        // When - save credentials
        val saveResult = repository.save(testCredentials)

        // Then - save succeeded
        assertTrue(saveResult.isSuccess)

        // When - retrieve credentials
        val retrievedCreds = repository.getCredentials()

        // Then - retrieved same credentials
        assertNotNull(retrievedCreds)
        assertEquals(testCredentials.email, retrievedCreds.email)
        assertEquals(testCredentials.password, retrievedCreds.password)
    }

    @Test
    fun `save, getCredentials, and delete flow works correctly`() = runTest {
        // Given
        every { mockCipher.encrypt(any()) } returns encryptedData
        every { mockSettings.putString(any(), any()) } just Runs
        every { mockSettings.getStringOrNull(CredentialsConstants.CREDENTIALS_KEY) } returnsMany listOf(
            encryptedData, // After save
            null // After delete
        )
        every { mockCipher.decrypt(encryptedData) } returns testCredentials
        every { mockCipher.decrypt(null) } returns null
        every { mockSettings.remove(any()) } just Runs

        // When - save
        repository.save(testCredentials)

        // Then - can retrieve
        val retrieved = repository.getCredentials()
        assertNotNull(retrieved)

        // When - delete
        repository.delete()

        // Then - no longer retrievable
        val afterDelete = repository.getCredentials()
        assertNull(afterDelete)
    }
}
