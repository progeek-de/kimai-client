package de.progeek.kimai.shared.core.mapper

import de.progeek.kimai.openapi.models.CustomerCollection
import de.progeek.kimai.shared.CustomerEntity
import de.progeek.kimai.shared.core.models.Customer

fun CustomerCollection.map(): Customer {
    return Customer(this.id?.toLong() ?: -1, this.name)
}

fun CustomerEntity.map(): Customer {
    return Customer(id = this.id, name = this.name)
}

fun de.progeek.kimai.openapi.models.Customer.toCustomer(): Customer {
    return Customer(
        id = id?.toLong() ?: -1,
        name = name,
    )
}