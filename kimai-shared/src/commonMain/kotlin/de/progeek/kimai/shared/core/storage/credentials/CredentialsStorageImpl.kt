package de.progeek.kimai.shared.core.storage.credentials

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import de.progeek.kimai.shared.core.models.Credentials
import de.progeek.kimai.shared.core.repositories.credentials.CredentialsRepository
import de.progeek.kimai.shared.core.storage.credentials.CredentialsConstants.CREDENTIALS_KEY
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CredentialsStorageImpl @OptIn(ExperimentalSettingsApi::class) constructor(
    private val settings: ObservableSettings,
    private val aesCipher: AesGCMCipher,
    private val flowSettings: FlowSettings = settings.toFlowSettings()
) : CredentialsRepository {

    override suspend fun save(cred: Credentials): Result<Unit> {
        val encrypted = aesCipher.encrypt(cred)
        val res = settings.putString(CREDENTIALS_KEY, encrypted)
        return Result.success(res)
    }

    @OptIn(ExperimentalSettingsApi::class)
    override fun get(): Flow<Credentials?> {
        return flowSettings.getStringOrNullFlow(CREDENTIALS_KEY).map {
            aesCipher.decrypt(it)
        }
    }

    override fun getCredentials(): Credentials? {
        val encrypted = settings.getStringOrNull(CREDENTIALS_KEY)
        return aesCipher.decrypt(encrypted)
    }

    override suspend fun delete(): Result<Unit> {
        val res = settings.remove(CREDENTIALS_KEY)
        return Result.success(res)
    }
}
