# UserApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getGetUser**](UserApi.md#getGetUser) | **GET** /api/users/{id} | Return one user entity
[**getGetUsers**](UserApi.md#getGetUsers) | **GET** /api/users | Returns the collection of users (which are visible to the user)
[**getMeUser**](UserApi.md#getMeUser) | **GET** /api/users/me | Return the current user entity
[**patchPatchUser**](UserApi.md#patchPatchUser) | **PATCH** /api/users/{id} | Update an existing user
[**postPostUser**](UserApi.md#postPostUser) | **POST** /api/users | Creates a new user


<a id="getGetUser"></a>
# **getGetUser**
> UserEntity getGetUser(id)

Return one user entity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = UserApi()
val id : kotlin.String = id_example // kotlin.String | User ID to fetch
try {
    val result : UserEntity = apiInstance.getGetUser(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UserApi#getGetUser")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UserApi#getGetUser")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| User ID to fetch |

### Return type

[**UserEntity**](UserEntity.md)

### Authorization


Configure apiToken:
    ApiClient.apiKey["X-AUTH-TOKEN"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-TOKEN"] = ""
Configure apiUser:
    ApiClient.apiKey["X-AUTH-USER"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-USER"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="getGetUsers"></a>
# **getGetUsers**
> kotlin.collections.List&lt;UserCollection&gt; getGetUsers(visible, orderBy, order, term)

Returns the collection of users (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = UserApi()
val visible : kotlin.String = visible_example // kotlin.String | Visibility status to filter users: 1=visible, 2=hidden, 3=all
val orderBy : kotlin.String = orderBy_example // kotlin.String | The field by which results will be ordered. Allowed values: id, username, alias, email (default: username)
val order : kotlin.String = order_example // kotlin.String | The result order. Allowed values: ASC, DESC (default: ASC)
val term : kotlin.String = term_example // kotlin.String | Free search term
try {
    val result : kotlin.collections.List<UserCollection> = apiInstance.getGetUsers(visible, orderBy, order, term)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UserApi#getGetUsers")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UserApi#getGetUsers")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **visible** | **kotlin.String**| Visibility status to filter users: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;all | [optional] [default to &quot;1&quot;]
 **orderBy** | **kotlin.String**| The field by which results will be ordered. Allowed values: id, username, alias, email (default: username) | [optional]
 **order** | **kotlin.String**| The result order. Allowed values: ASC, DESC (default: ASC) | [optional]
 **term** | **kotlin.String**| Free search term | [optional]

### Return type

[**kotlin.collections.List&lt;UserCollection&gt;**](UserCollection.md)

### Authorization


Configure apiToken:
    ApiClient.apiKey["X-AUTH-TOKEN"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-TOKEN"] = ""
Configure apiUser:
    ApiClient.apiKey["X-AUTH-USER"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-USER"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="getMeUser"></a>
# **getMeUser**
> UserEntity getMeUser()

Return the current user entity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = UserApi()
try {
    val result : UserEntity = apiInstance.getMeUser()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UserApi#getMeUser")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UserApi#getMeUser")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**UserEntity**](UserEntity.md)

### Authorization


Configure apiToken:
    ApiClient.apiKey["X-AUTH-TOKEN"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-TOKEN"] = ""
Configure apiUser:
    ApiClient.apiKey["X-AUTH-USER"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-USER"] = ""

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a id="patchPatchUser"></a>
# **patchPatchUser**
> UserEntity patchPatchUser(id, userEditForm)

Update an existing user

Update an existing user, you can pass all or just a subset of all attributes (passing roles will replace all existing ones)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = UserApi()
val id : kotlin.String = id_example // kotlin.String | User ID to update
val userEditForm : UserEditForm =  // UserEditForm | 
try {
    val result : UserEntity = apiInstance.patchPatchUser(id, userEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling UserApi#patchPatchUser")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UserApi#patchPatchUser")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| User ID to update |
 **userEditForm** | [**UserEditForm**](UserEditForm.md)|  |

### Return type

[**UserEntity**](UserEntity.md)

### Authorization


Configure apiToken:
    ApiClient.apiKey["X-AUTH-TOKEN"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-TOKEN"] = ""
Configure apiUser:
    ApiClient.apiKey["X-AUTH-USER"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-USER"] = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a id="postPostUser"></a>
# **postPostUser**
> postPostUser(userCreateForm)

Creates a new user

Creates a new user and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = UserApi()
val userCreateForm : UserCreateForm =  // UserCreateForm | 
try {
    apiInstance.postPostUser(userCreateForm)
} catch (e: ClientException) {
    println("4xx response calling UserApi#postPostUser")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling UserApi#postPostUser")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **userCreateForm** | [**UserCreateForm**](UserCreateForm.md)|  |

### Return type

null (empty response body)

### Authorization


Configure apiToken:
    ApiClient.apiKey["X-AUTH-TOKEN"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-TOKEN"] = ""
Configure apiUser:
    ApiClient.apiKey["X-AUTH-USER"] = ""
    ApiClient.apiKeyPrefix["X-AUTH-USER"] = ""

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: Not defined

