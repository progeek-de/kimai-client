package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.ActivityCollection
import de.progeek.kimai.shared.ActivityEntity
import de.progeek.kimai.shared.core.models.Activity

fun ActivityEntity.map(): Activity {
    return Activity(
        id = this.id,
        name = this.name,
        project = this.project
    )
}

fun ActivityCollection.map(): Activity {
    return Activity(
        id = this.id?.toLong() ?: -1,
        name = this.name,
        project = this.project?.toLong()
    )
}

fun de.progeek.kimai.openapi.models.ActivityExpanded.toActivity(): Activity {
    return Activity(
        id = id?.toLong() ?: -1,
        name = name,
        project = project?.toProject()?.id
    )
}