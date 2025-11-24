package de.progeek.kimai.shared.core.storage.credentials

import de.progeek.kimai.shared.core.models.Credentials
import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.symmetric.AES
import dev.whyoleg.cryptography.operations.cipher.AuthenticatedCipher

class AesGCMCipher {

    private var cipher: AuthenticatedCipher

    init {
        cipher = buildCipher(CredentialsConstants.KIMAI_SECRET)
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun buildCipher(encryptedKey: String): AuthenticatedCipher {
        return CryptographyProvider.Default
            .get(AES.GCM)
            .keyDecoder()
            .decodeFromBlocking(AES.Key.Format.RAW, encryptedKey.hexToByteArray())
            .cipher()
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun encrypt(credentials: Credentials): String {
        val raw = "${credentials.email}:${credentials.password}"
        return cipher.encryptBlocking(plaintextInput = raw.encodeToByteArray())
            .toHexString(HexFormat.Default)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun decrypt(str: String?): Credentials? {
        return str?.let {
            val raw = cipher.decryptBlocking(ciphertextInput = it.hexToByteArray()).decodeToString()
            val credentialsParts = raw.split(":")
            if (credentialsParts.size == 2) {
                val login = credentialsParts[0]
                val password = credentialsParts[1]
                return Credentials(login, password)
            }

            return null
        }
    }

    /**
     * Encrypt a plain string value
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun encryptString(plaintext: String): String {
        return cipher.encryptBlocking(plaintextInput = plaintext.encodeToByteArray())
            .toHexString(HexFormat.Default)
    }

    /**
     * Decrypt an encrypted string value
     */
    @OptIn(ExperimentalStdlibApi::class)
    fun decryptString(encrypted: String): String? {
        return try {
            cipher.decryptBlocking(ciphertextInput = encrypted.hexToByteArray()).decodeToString()
        } catch (e: Exception) {
            null
        }
    }

}