package de.progeek.kimai.shared.core.network.client

import com.russhwolf.settings.ObservableSettings
import de.progeek.kimai.openapi.apis.ProjectApi
import de.progeek.kimai.shared.core.mapper.map
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.core.storage.credentials.AesGCMCipher

class ProjectClient(
    settings: ObservableSettings,
    aesCipher: AesGCMCipher,
    private val client: ProjectApi,
): CredentialsListener(settings, aesCipher, client) {

    suspend fun getProjects(): Result<List<Project>> {
        return kotlin.runCatching {
            val response = client.getGetProjects()
            if (!response.success) {
                throw Throwable("Failed load projects")
            }

            response.body().map { it.map() }
        }
    }
}