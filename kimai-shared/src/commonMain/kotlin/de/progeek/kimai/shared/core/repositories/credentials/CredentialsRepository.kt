package de.progeek.kimai.shared.core.repositories.credentials

import de.progeek.kimai.shared.core.models.Credentials
import kotlinx.coroutines.flow.Flow

interface CredentialsRepository {
    suspend fun save(cred: Credentials): Result<Unit>
    fun get(): Flow<Credentials?>
    fun getCredentials(): Credentials?
    suspend fun delete(): Result<Unit>
}