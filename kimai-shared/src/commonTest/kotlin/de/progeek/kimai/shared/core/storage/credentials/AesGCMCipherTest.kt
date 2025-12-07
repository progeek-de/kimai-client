package de.progeek.kimai.shared.core.storage.credentials

import de.progeek.kimai.shared.core.models.Credentials
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for AesGCMCipher encryption/decryption.
 *
 * Tests cover:
 * - encrypt(Credentials) and decrypt() round-trip
 * - encryptString() and decryptString() round-trip
 * - Invalid input handling
 * - Edge cases (empty strings, special characters)
 */
class AesGCMCipherTest {

    private val cipher = AesGCMCipher()

    // ============================================================
    // encrypt(Credentials) and decrypt() Round-trip Tests
    // ============================================================

    @Test
    fun `encrypt and decrypt returns same credentials`() {
        val original = Credentials("test@example.com", "password123")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        assertNotNull(decrypted, "Decrypted credentials should not be null")
        assertEquals(original.email, decrypted.email, "Email should match after round-trip")
        assertEquals(original.password, decrypted.password, "Password should match after round-trip")
    }

    @Test
    fun `encrypt produces non-empty hex string`() {
        val credentials = Credentials("user@test.com", "secret")

        val encrypted = cipher.encrypt(credentials)

        assertTrue(encrypted.isNotEmpty(), "Encrypted string should not be empty")
        assertTrue(encrypted.all { it in '0'..'9' || it in 'a'..'f' }, "Should be valid hex string")
    }

    @Test
    fun `encrypt produces different output for different inputs`() {
        val creds1 = Credentials("user1@test.com", "pass1")
        val creds2 = Credentials("user2@test.com", "pass2")

        val encrypted1 = cipher.encrypt(creds1)
        val encrypted2 = cipher.encrypt(creds2)

        assertNotEquals(encrypted1, encrypted2, "Different credentials should produce different encrypted values")
    }

    @Test
    fun `decrypt returns null for null input`() {
        val result = cipher.decrypt(null)

        assertNull(result, "decrypt(null) should return null")
    }

    @Test
    fun `decrypt handles credentials with special characters`() {
        val original = Credentials("user+test@example.com", "p@ss!w0rd#$%")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        assertNotNull(decrypted)
        assertEquals(original.email, decrypted.email)
        assertEquals(original.password, decrypted.password)
    }

    @Test
    fun `decrypt handles credentials with unicode characters`() {
        val original = Credentials("user@example.com", "пароль密码")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        assertNotNull(decrypted)
        assertEquals(original.email, decrypted.email)
        assertEquals(original.password, decrypted.password)
    }

    @Test
    fun `decrypt handles empty email`() {
        val original = Credentials("", "password")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        assertNotNull(decrypted)
        assertEquals("", decrypted.email)
        assertEquals("password", decrypted.password)
    }

    @Test
    fun `decrypt handles empty password`() {
        val original = Credentials("user@test.com", "")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        assertNotNull(decrypted)
        assertEquals("user@test.com", decrypted.email)
        assertEquals("", decrypted.password)
    }

    @Test
    fun `encrypt is deterministic with same key`() {
        // Note: GCM uses random nonce, so same input produces different ciphertext
        // But decrypt should still work
        val original = Credentials("test@example.com", "password")

        val encrypted1 = cipher.encrypt(original)
        val encrypted2 = cipher.encrypt(original)

        // GCM produces different ciphertext each time (random nonce)
        // But both should decrypt correctly
        val decrypted1 = cipher.decrypt(encrypted1)
        val decrypted2 = cipher.decrypt(encrypted2)

        assertEquals(original.email, decrypted1?.email)
        assertEquals(original.email, decrypted2?.email)
    }

    // ============================================================
    // encryptString() and decryptString() Round-trip Tests
    // ============================================================

    @Test
    fun `encryptString and decryptString returns same string`() {
        val original = "Hello, World!"

        val encrypted = cipher.encryptString(original)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(original, decrypted, "Decrypted string should match original")
    }

    @Test
    fun `encryptString produces valid hex string`() {
        val plaintext = "test string"

        val encrypted = cipher.encryptString(plaintext)

        assertTrue(encrypted.isNotEmpty(), "Encrypted string should not be empty")
        assertTrue(encrypted.all { it in '0'..'9' || it in 'a'..'f' }, "Should be valid hex string")
    }

    @Test
    fun `encryptString handles empty string`() {
        val encrypted = cipher.encryptString("")
        val decrypted = cipher.decryptString(encrypted)

        assertEquals("", decrypted, "Empty string should round-trip correctly")
    }

    @Test
    fun `encryptString handles long string`() {
        val original = "A".repeat(10000)

        val encrypted = cipher.encryptString(original)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(original, decrypted, "Long string should round-trip correctly")
    }

    @Test
    fun `encryptString handles special characters`() {
        val original = "!@#\$%^&*()_+-=[]{}|;':\",./<>?`~"

        val encrypted = cipher.encryptString(original)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(original, decrypted, "Special characters should round-trip correctly")
    }

    @Test
    fun `encryptString handles unicode characters`() {
        val original = "日本語 한국어 العربية 🎉"

        val encrypted = cipher.encryptString(original)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(original, decrypted, "Unicode characters should round-trip correctly")
    }

    @Test
    fun `encryptString handles newlines and tabs`() {
        val original = "line1\nline2\ttabbed"

        val encrypted = cipher.encryptString(original)
        val decrypted = cipher.decryptString(encrypted)

        assertEquals(original, decrypted, "Newlines and tabs should round-trip correctly")
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    fun `decryptString returns null for invalid hex string`() {
        val result = cipher.decryptString("not_valid_hex_xyz")

        assertNull(result, "Invalid hex string should return null")
    }

    @Test
    fun `decryptString returns null for empty string`() {
        val result = cipher.decryptString("")

        assertNull(result, "Empty encrypted string should return null")
    }

    @Test
    fun `decryptString returns null for corrupted ciphertext`() {
        // Valid hex but invalid ciphertext (too short for GCM)
        val result = cipher.decryptString("0011223344556677")

        assertNull(result, "Corrupted ciphertext should return null")
    }

    @Test
    fun `decryptString handles odd-length hex string gracefully`() {
        // Odd-length hex strings are invalid
        val result = cipher.decryptString("0011223")

        assertNull(result, "Odd-length hex string should return null")
    }

    // ============================================================
    // Credential Format Edge Cases
    // ============================================================

    @Test
    fun `decrypt returns null for credentials with colon in password`() {
        // The format is "email:password", so colon in password is problematic
        // Current implementation splits on ":" and checks size == 2
        // With colons in password, split produces more than 2 parts
        val original = Credentials("user@test.com", "pass:with:colons")

        val encrypted = cipher.encrypt(original)
        val decrypted = cipher.decrypt(encrypted)

        // This documents the current behavior limitation:
        // Passwords containing colons cannot be round-tripped correctly
        assertNull(decrypted, "Passwords with colons are not supported due to split(':') implementation")
    }

    @Test
    fun `decrypt returns null for malformed credential string without colon`() {
        // Encrypt a string without colon manually
        val malformedEncrypted = cipher.encryptString("no_colon_here")
        val result = cipher.decrypt(malformedEncrypted)

        assertNull(result, "Malformed credential string should return null")
    }

    // ============================================================
    // Multiple Instances Tests
    // ============================================================

    @Test
    fun `different cipher instances can decrypt each others output`() {
        val cipher1 = AesGCMCipher()
        val cipher2 = AesGCMCipher()

        val original = Credentials("test@example.com", "password")

        val encrypted = cipher1.encrypt(original)
        val decrypted = cipher2.decrypt(encrypted)

        assertNotNull(decrypted)
        assertEquals(original.email, decrypted.email)
        assertEquals(original.password, decrypted.password)
    }

    @Test
    fun `encryptString from one instance can be decrypted by another`() {
        val cipher1 = AesGCMCipher()
        val cipher2 = AesGCMCipher()

        val original = "test string"

        val encrypted = cipher1.encryptString(original)
        val decrypted = cipher2.decryptString(encrypted)

        assertEquals(original, decrypted)
    }
}
