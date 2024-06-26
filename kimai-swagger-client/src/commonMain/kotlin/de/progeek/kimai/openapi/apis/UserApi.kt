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

import de.progeek.kimai.openapi.models.UserCollection
import de.progeek.kimai.openapi.models.UserCreateForm
import de.progeek.kimai.openapi.models.UserEditForm
import de.progeek.kimai.openapi.models.UserEntity

import de.progeek.kimai.openapi.infrastructure.*
import io.ktor.client.HttpClientConfig
import io.ktor.client.engine.HttpClientEngine
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

open class UserApi(
    baseUrl: String = ApiClient.BASE_URL,
    httpClientEngine: HttpClientEngine? = null,
    httpClientConfig: ((HttpClientConfig<*>) -> Unit)? = null,
    jsonSerializer: Json = ApiClient.JSON_DEFAULT
) : ApiClient(baseUrl, httpClientEngine, httpClientConfig, jsonSerializer) {

    /**
     * Return one user entity
     * 
     * @param id User ID to fetch
     * @return UserEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetUser(id: kotlin.String): HttpResponse<UserEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/users/{id}".replace("{" + "id" + "}", "$id"),
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
     * Returns the collection of users (which are visible to the user)
     * 
     * @param visible Visibility status to filter users: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;all (optional, default to "1")
     * @param orderBy The field by which results will be ordered. Allowed values: id, username, alias, email (default: username) (optional)
     * @param order The result order. Allowed values: ASC, DESC (default: ASC) (optional)
     * @param term Free search term (optional)
     * @return kotlin.collections.List<UserCollection>
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getGetUsers(visible: kotlin.String? = "1", orderBy: kotlin.String? = null, order: kotlin.String? = null, term: kotlin.String? = null): HttpResponse<kotlin.collections.List<UserCollection>> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        visible?.apply { localVariableQuery["visible"] = listOf("$visible") }
        orderBy?.apply { localVariableQuery["orderBy"] = listOf("$orderBy") }
        order?.apply { localVariableQuery["order"] = listOf("$order") }
        term?.apply { localVariableQuery["term"] = listOf("$term") }
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/users",
            query = localVariableQuery,
            headers = localVariableHeaders,
            requiresAuthentication = true,
        )

        return request(
            localVariableConfig,
            localVariableBody,
            localVariableAuthNames
        ).wrap<GetGetUsersResponse>().map { value }
    }

    @Serializable
    private class GetGetUsersResponse(val value: List<UserCollection>) {
        @Serializer(GetGetUsersResponse::class)
        companion object : KSerializer<GetGetUsersResponse> {
            private val serializer: KSerializer<List<UserCollection>> = serializer<List<UserCollection>>()
            override val descriptor = serializer.descriptor
            override fun serialize(encoder: Encoder, obj: GetGetUsersResponse) = serializer.serialize(encoder, obj.value)
            override fun deserialize(decoder: Decoder) = GetGetUsersResponse(serializer.deserialize(decoder))
        }
    }

    /**
     * Return the current user entity
     * 
     * @return UserEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun getMeUser(): HttpResponse<UserEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = 
            io.ktor.client.utils.EmptyContent

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.GET,
            "/api/users/me",
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
     * Update an existing user
     * Update an existing user, you can pass all or just a subset of all attributes (passing roles will replace all existing ones)
     * @param id User ID to update
     * @param userEditForm 
     * @return UserEntity
     */
    @Suppress("UNCHECKED_CAST")
    open suspend fun patchPatchUser(id: kotlin.String, userEditForm: UserEditForm): HttpResponse<UserEntity> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = userEditForm

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.PATCH,
            "/api/users/{id}".replace("{" + "id" + "}", "$id"),
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
     * Creates a new user
     * Creates a new user and returns it afterwards
     * @param userCreateForm 
     * @return void
     */
    open suspend fun postPostUser(userCreateForm: UserCreateForm): HttpResponse<Unit> {

        val localVariableAuthNames = listOf<String>("apiToken", "apiUser")

        val localVariableBody = userCreateForm

        val localVariableQuery = mutableMapOf<String, List<String>>()
        val localVariableHeaders = mutableMapOf<String, String>()

        val localVariableConfig = RequestConfig<kotlin.Any?>(
            RequestMethod.POST,
            "/api/users",
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
