/**
 *
 * Please note:
 * This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * Do not edit this file manually.
 *
 */

@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport"
)

package de.progeek.kimai.openapi.apis

import de.progeek.kimai.openapi.models.CustomerCollection
import de.progeek.kimai.openapi.models.CustomerEditForm
import de.progeek.kimai.openapi.models.CustomerEntity
import de.progeek.kimai.openapi.models.CustomerRate
import de.progeek.kimai.openapi.models.CustomerRateForm
import de.progeek.kimai.openapi.models.PatchAppApiActivityMetaRequest

import de.progeek.kimai.openapi.infrastructure.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

open class CustomerApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     * Deletes one rate for a customer
     * 
     * @param id The customer whose rate will be removed
     * @param rateId The rate to remove
     * @return void
     */
    open suspend fun deleteDeleteCustomerRate(id: kotlin.String, rateId: kotlin.String): HttpResponse<Unit> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.DELETE,
            "/api/customers/{id}/rates/{rateId}".replace("{" + "id" + "}", "$id").replace("{" + "rateId" + "}", "$rateId"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }


    /**
     * Returns one customer
     * 
     * @param id 
     * @return CustomerEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetCustomer(id: kotlin.String): HttpResponse<CustomerEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/customers/{id}".replace("{" + "id" + "}", "$id"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }


    /**
     * Returns a collection of all rates for one customer
     * 
     * @param id The customer whose rates will be returned
     * @return kotlin.collections.List<CustomerRate>
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetCustomerRates(id: kotlin.String): HttpResponse<kotlin.collections.List<CustomerRate>> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/customers/{id}/rates".replace("{" + "id" + "}", "$id"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap<GetGetCustomerRatesResponse>().map { value }
    }

    @Serializable
    private class GetGetCustomerRatesResponse(val value: List<CustomerRate>) {
        @Serializer(GetGetCustomerRatesResponse::class)
        companion object : KSerializer<GetGetCustomerRatesResponse> {
            private val serializer: KSerializer<List<CustomerRate>> = serializer<List<CustomerRate>>()
            override val descriptor = serializer.descriptor
            override fun serialize(encoder: Encoder, obj: GetGetCustomerRatesResponse) = serializer.serialize(encoder, obj.value)
            override fun deserialize(decoder: Decoder) = GetGetCustomerRatesResponse(serializer.deserialize(decoder))
        }
    }

    /**
     * Returns a collection of customers (which are visible to the user)
     * 
     * @param visible Visibility status to filter customers: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;both (optional, default to "1")
     * @param order The result order. Allowed values: ASC, DESC (default: ASC) (optional)
     * @param orderBy The field by which results will be ordered. Allowed values: id, name (default: name) (optional)
     * @param term Free search term (optional)
     * @return kotlin.collections.List<CustomerCollection>
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetCustomers(visible: kotlin.String? = "1", order: kotlin.String? = null, orderBy: kotlin.String? = null, term: kotlin.String? = null): HttpResponse<kotlin.collections.List<CustomerCollection>> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        visible?.apply { localVariableQuery["visible"] = listOf("$visible") }
        order?.apply { localVariableQuery["order"] = listOf("$order") }
        orderBy?.apply { localVariableQuery["orderBy"] = listOf("$orderBy") }
        term?.apply { localVariableQuery["term"] = listOf("$term") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/customers",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap<GetGetCustomersResponse>().map { value }
    }

    @Serializable
    private class GetGetCustomersResponse(val value: List<CustomerCollection>) {
        @Serializer(GetGetCustomersResponse::class)
        companion object : KSerializer<GetGetCustomersResponse> {
            private val serializer: KSerializer<List<CustomerCollection>> = serializer<List<CustomerCollection>>()
            override val descriptor = serializer.descriptor
            override fun serialize(encoder: Encoder, obj: GetGetCustomersResponse) = serializer.serialize(encoder, obj.value)
            override fun deserialize(decoder: Decoder) = GetGetCustomersResponse(serializer.deserialize(decoder))
        }
    }

    /**
     * Sets the value of a meta-field for an existing customer
     * 
     * @param id Customer record ID to set the meta-field value for
     * @param patchAppApiActivityMetaRequest  (optional)
     * @return CustomerEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun patchAppApiCustomerMeta(id: kotlin.String, patchAppApiActivityMetaRequest: PatchAppApiActivityMetaRequest? = null): HttpResponse<CustomerEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = patchAppApiActivityMetaRequest

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.PATCH,
            "/api/customers/{id}/meta".replace("{" + "id" + "}", "$id"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }



    /**
     * Update an existing customer
     * Update an existing customer, you can pass all or just a subset of all attributes
     * @param id Customer ID to update
     * @param customerEditForm 
     * @return CustomerEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun patchPatchCustomer(id: kotlin.String, customerEditForm: CustomerEditForm): HttpResponse<CustomerEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = customerEditForm

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.PATCH,
            "/api/customers/{id}".replace("{" + "id" + "}", "$id"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }



    /**
     * Creates a new customer
     * Creates a new customer and returns it afterwards
     * @param customerEditForm 
     * @return CustomerEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun postPostCustomer(customerEditForm: CustomerEditForm): HttpResponse<CustomerEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = customerEditForm

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/api/customers",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }



    /**
     * Adds a new rate to a customer
     * 
     * @param id The customer to add the rate for
     * @param customerRateForm 
     * @return CustomerRate
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun postPostCustomerRate(id: kotlin.String, customerRateForm: CustomerRateForm): HttpResponse<CustomerRate> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = customerRateForm

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/api/customers/{id}/rates".replace("{" + "id" + "}", "$id"),
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return jsonRequest(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap()
    }



}
