package de.progeek.kimai.shared.core.repositories.customer

import de.progeek.kimai.shared.core.database.datasource.customer.CustomerDatasource
import de.progeek.kimai.shared.core.models.Customer
import de.progeek.kimai.shared.core.network.client.CustomerClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.mobilenativefoundation.store.store5.*
import org.mobilenativefoundation.store.store5.impl.extensions.fresh

class CustomerRepository(
    private val customerClient: CustomerClient,
    private val customerDataSource: CustomerDatasource
) : KoinComponent {

    private val store = StoreBuilder
        .from(
            fetcher = Fetcher.of {
                customerClient.getCustomers().getOrThrow()
            },
            sourceOfTruth = SourceOfTruth.of(
                reader = {_ -> customerDataSource.getAll()},
                writer = { _, list -> customerDataSource.insert(list)},
                deleteAll = {customerDataSource.deleteAll()}
            ),
        ).build()

    fun getCustomers(): Flow<List<Customer>> = store.stream(
            StoreReadRequest.cached("customers::all", false)
        ).map { it.dataOrNull() ?: emptyList() }


    @OptIn(ExperimentalStoreApi::class)
    suspend fun invalidateCache() {
        store.clear()
        store.fresh("customers::all")
    }
}