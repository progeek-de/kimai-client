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

import de.progeek.kimai.openapi.models.PageAction

import de.progeek.kimai.openapi.infrastructure.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

open class ActionsApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     * Get all item actions for the given Activity [for internal use]
     * 
     * @param id Activity ID to fetch
     * @param view View to display the actions at (e.g. index, custom)
     * @param locale Language to translate the action title to (e.g. de, en)
     * @return PageAction
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetActivityActions(id: kotlin.String, view: kotlin.String, locale: kotlin.String): HttpResponse<PageAction> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/actions/activity/{id}/{view}/{locale}".replace("{" + "id" + "}", "$id").replace("{" + "view" + "}", "$view").replace("{" + "locale" + "}", "$locale"),
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
     * Get all item actions for the given Customer [for internal use]
     * 
     * @param id Customer ID to fetch
     * @param view View to display the actions at (e.g. index, custom)
     * @param locale Language to translate the action title to (e.g. de, en)
     * @return PageAction
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetCustomerActions(id: kotlin.String, view: kotlin.String, locale: kotlin.String): HttpResponse<PageAction> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/actions/customer/{id}/{view}/{locale}".replace("{" + "id" + "}", "$id").replace("{" + "view" + "}", "$view").replace("{" + "locale" + "}", "$locale"),
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
     * Get all item actions for the given Project [for internal use]
     * 
     * @param id Project ID to fetch
     * @param view View to display the actions at (e.g. index, custom)
     * @param locale Language to translate the action title to (e.g. de, en)
     * @return PageAction
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetProjectActions(id: kotlin.String, view: kotlin.String, locale: kotlin.String): HttpResponse<PageAction> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/actions/project/{id}/{view}/{locale}".replace("{" + "id" + "}", "$id").replace("{" + "view" + "}", "$view").replace("{" + "locale" + "}", "$locale"),
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
     * Get all item actions for the given Timesheet [for internal use]
     * 
     * @param id Timesheet ID to fetch
     * @param view View to display the actions at (e.g. index, custom)
     * @param locale Language to translate the action title to (e.g. de, en)
     * @return PageAction
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetTimesheetActions(id: kotlin.String, view: kotlin.String, locale: kotlin.String): HttpResponse<PageAction> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/actions/timesheet/{id}/{view}/{locale}".replace("{" + "id" + "}", "$id").replace("{" + "view" + "}", "$view").replace("{" + "locale" + "}", "$locale"),
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


}