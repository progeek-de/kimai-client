package de.progeek.kimai.shared.core.database.datasource.customer

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import de.progeek.kimai.shared.core.database.KimaiDatabase
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.kimaiDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CustomerDatasource(val database: KimaiDatabase) {
    private val query get() = database.customerEntityQueries

    private inline fun toCustomer(id: Long, name: String): Customer =
        Customer(id, name)

    fun getAll(): Flow<List<Customer>> =
        query.getAllCustomers(::toCustomer).asFlow().mapToList(kimaiDispatchers.io)

    suspend fun insert(list: List<Customer>): Result<List<Customer>> =
        withContext(kimaiDispatchers.io) {
            runCatching {
                query.transaction {
                    list.forEach { item ->
                        query.insertCustomer(
                            id = item.id,
                            name = item.name,
                        )
                    }
                }
                list
            }
        }

    fun deleteAll() = query.deleteAll()
}