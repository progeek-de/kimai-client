package de.progeek.kimai.shared.core.database.datasource.project

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.mapper.map
import de.progeek.kimai.shared.core.mapper.toProject
import de.progeek.kimai.shared.core.models.Project
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProjectDatasource(val database: KimaiDatabase) {

    private val query get() = database.projectEntityQueries

    fun getAll(): Flow<List<Project>> =
        query.getAllProjects(::toProject)
            .asFlow()
            .mapToList(kimaiDispatchers.io)


    suspend fun getById(id: Int): Result<Project?> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.getProjectById(id.toLong()).executeAsOneOrNull()?.map()
        }
    }

    suspend fun insert(project: Project): Result<Unit> = withContext(kimaiDispatchers.io) {
        runCatching {
            query.insertProject(
                project.id,
                project.parent,
                project.name,
                if (project.globalActivities) 1 else 0,
                project.customer?.id ?: -1
            )
        }
    }

    suspend fun insert(projects: List<Project>): Result<List<Project>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.transaction {
                    projects.forEach { project ->
                        query.insertProject(
                            project.id,
                            project.parent,
                            project.name,
                            if (project.globalActivities) 1 else 0,
                            project.customer?.id ?: -1
                        )
                    }
                }
                projects
            }
        }

    fun deleteAll() = query.deleteAll()
}