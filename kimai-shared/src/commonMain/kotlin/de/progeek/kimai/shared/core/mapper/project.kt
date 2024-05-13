package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.ProjectCollection
import de.progeek.kimai.openapi.models.ProjectEntity
import de.progeek.kimai.openapi.models.ProjectExpanded
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.models.Project

fun ProjectCollection.map(): Project {
    return Project(
        id = this.id?.toLong() ?: -1,
        name = this.name,
        parent = this.parentTitle ?: "",
        globalActivities = this.globalActivities,
        customer = Customer(id = this.customer?.toLong() ?: -1, name = this.parentTitle ?: "")
    )
}

fun ProjectEntity.map(): Project {
    return Project(
        id = this.id?.toLong() ?: -1,
        name = this.name,
        parent = this.parentTitle ?: "",
        globalActivities = this.globalActivities,
        customer = Customer(id = this.customer?.toLong() ?: -1, name = this.parentTitle ?: "")
    )
}

fun de.progeek.kimai.shared.ProjectEntity.map(): Project {
    return Project(
        id = this.id,
        name = this.name,
        parent = this.parent,
        globalActivities = this.globalActivities.toInt() == 1,
        customer = Customer(id = this.customer, name = this.parent ?: "")
    )
}

fun toProject(id: Long, parent: String, name: String, globalActivities: Long, customer: Long): Project =
    Project(
        id = id,
        name = name,
        parent = parent,
        globalActivities = globalActivities.toInt() == 1,
        customer = Customer(id = customer, name = parent ?: "")
    )

fun ProjectExpanded.toProject(): Project {
    return Project(
        id = id?.toLong() ?: -1,
        name = name,
        globalActivities = globalActivities,
        customer = customer.toCustomer(),
        parent = ""
    )
}