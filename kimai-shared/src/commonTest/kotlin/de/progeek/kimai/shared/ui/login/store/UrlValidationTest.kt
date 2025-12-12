package de.progeek.kimai.shared.ui.login.store

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class UrlValidationTest {

    @Test
    fun `valid https url with domain returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.com"))
    }

    @Test
    fun `valid http url with domain returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://example.com"))
    }

    @Test
    fun `valid url with subdomain returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://api.example.com"))
    }

    @Test
    fun `valid url with multiple subdomains returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://api.v1.example.com"))
    }

    @Test
    fun `valid url with port returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.com:8080"))
    }

    @Test
    fun `valid url with path returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.com/api"))
    }

    @Test
    fun `valid url with port and path returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.com:8080/api/v1"))
    }

    @Test
    fun `valid localhost url returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://localhost"))
    }

    @Test
    fun `valid localhost url with port returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://localhost:3000"))
    }

    @Test
    fun `valid localhost url with path returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://localhost:8080/api"))
    }

    @Test
    fun `valid IP address url returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://192.168.1.1"))
    }

    @Test
    fun `valid IP address url with port returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("http://192.168.1.1:8080"))
    }

    @Test
    fun `valid IP address url with path returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://10.0.0.1:443/kimai"))
    }

    @Test
    fun `valid kimai cloud url returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://demo.kimai.org"))
    }

    @Test
    fun `valid url with hyphen in domain returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://my-kimai-server.example.com"))
    }

    @Test
    fun `empty string returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl(""))
    }

    @Test
    fun `blank string returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("   "))
    }

    @Test
    fun `url without protocol returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("example.com"))
    }

    @Test
    fun `url without protocol but with www returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("www.example.com"))
    }

    @Test
    fun `ftp protocol returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("ftp://example.com"))
    }

    @Test
    fun `invalid protocol returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("htt://example.com"))
    }

    @Test
    fun `just protocol returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://"))
    }

    @Test
    fun `random text returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("not a url"))
    }

    @Test
    fun `single word returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("kimai"))
    }

    @Test
    fun `url with invalid port returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://example.com:999999"))
    }

    @Test
    fun `url with spaces returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://example .com"))
    }

    @Test
    fun `url with leading spaces is trimmed and valid`() {
        assertTrue(LoginStoreFactory.isValidUrl("  https://example.com"))
    }

    @Test
    fun `url with trailing spaces is trimmed and valid`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.com  "))
    }

    @Test
    fun `domain starting with hyphen returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://-example.com"))
    }

    @Test
    fun `domain ending with hyphen returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://example-.com"))
    }

    @Test
    fun `localhost https returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://localhost:8443"))
    }

    @Test
    fun `valid two-letter tld returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.de"))
    }

    @Test
    fun `valid long tld returns true`() {
        assertTrue(LoginStoreFactory.isValidUrl("https://example.cloud"))
    }

    @Test
    fun `single letter tld returns false`() {
        assertFalse(LoginStoreFactory.isValidUrl("https://example.c"))
    }
}
