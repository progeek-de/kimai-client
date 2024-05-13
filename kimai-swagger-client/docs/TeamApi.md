# TeamApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteTeam**](TeamApi.md#deleteDeleteTeam) | **DELETE** /api/teams/{id} | Delete a team
[**deleteDeleteTeamActivity**](TeamApi.md#deleteDeleteTeamActivity) | **DELETE** /api/teams/{id}/activities/{activityId} | Revokes access for an activity from a team
[**deleteDeleteTeamCustomer**](TeamApi.md#deleteDeleteTeamCustomer) | **DELETE** /api/teams/{id}/customers/{customerId} | Revokes access for a customer from a team
[**deleteDeleteTeamMember**](TeamApi.md#deleteDeleteTeamMember) | **DELETE** /api/teams/{id}/members/{userId} | Removes a member from the team
[**deleteDeleteTeamProject**](TeamApi.md#deleteDeleteTeamProject) | **DELETE** /api/teams/{id}/projects/{projectId} | Revokes access for a project from a team
[**getGetTeam**](TeamApi.md#getGetTeam) | **GET** /api/teams/{id} | Returns one team
[**getGetTeams**](TeamApi.md#getGetTeams) | **GET** /api/teams | Fetch all existing teams (which are visible to the user)
[**patchPatchTeam**](TeamApi.md#patchPatchTeam) | **PATCH** /api/teams/{id} | Update an existing team
[**postPostTeam**](TeamApi.md#postPostTeam) | **POST** /api/teams | Creates a new team
[**postPostTeamActivity**](TeamApi.md#postPostTeamActivity) | **POST** /api/teams/{id}/activities/{activityId} | Grant the team access to an activity
[**postPostTeamCustomer**](TeamApi.md#postPostTeamCustomer) | **POST** /api/teams/{id}/customers/{customerId} | Grant the team access to a customer
[**postPostTeamMember**](TeamApi.md#postPostTeamMember) | **POST** /api/teams/{id}/members/{userId} | Add a new member to a team
[**postPostTeamProject**](TeamApi.md#postPostTeamProject) | **POST** /api/teams/{id}/projects/{projectId} | Grant the team access to a project


<a id="deleteDeleteTeam"></a>
# **deleteDeleteTeam**
> deleteDeleteTeam(id)

Delete a team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | Team ID to delete
try {
    apiInstance.deleteDeleteTeam(id)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#deleteDeleteTeam")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#deleteDeleteTeam")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Team ID to delete |

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

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a id="deleteDeleteTeamActivity"></a>
# **deleteDeleteTeamActivity**
> Team deleteDeleteTeamActivity(id, activityId)

Revokes access for an activity from a team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team whose permission will be revoked
val activityId : kotlin.String = activityId_example // kotlin.String | The activity to remove (Activity ID)
try {
    val result : Team = apiInstance.deleteDeleteTeamActivity(id, activityId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#deleteDeleteTeamActivity")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#deleteDeleteTeamActivity")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team whose permission will be revoked |
 **activityId** | **kotlin.String**| The activity to remove (Activity ID) |

### Return type

[**Team**](Team.md)

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

<a id="deleteDeleteTeamCustomer"></a>
# **deleteDeleteTeamCustomer**
> Team deleteDeleteTeamCustomer(id, customerId)

Revokes access for a customer from a team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team whose permission will be revoked
val customerId : kotlin.String = customerId_example // kotlin.String | The customer to remove (Customer ID)
try {
    val result : Team = apiInstance.deleteDeleteTeamCustomer(id, customerId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#deleteDeleteTeamCustomer")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#deleteDeleteTeamCustomer")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team whose permission will be revoked |
 **customerId** | **kotlin.String**| The customer to remove (Customer ID) |

### Return type

[**Team**](Team.md)

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

<a id="deleteDeleteTeamMember"></a>
# **deleteDeleteTeamMember**
> Team deleteDeleteTeamMember(id, userId)

Removes a member from the team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team from which the member will be removed
val userId : kotlin.String = userId_example // kotlin.String | The team member to remove (User ID)
try {
    val result : Team = apiInstance.deleteDeleteTeamMember(id, userId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#deleteDeleteTeamMember")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#deleteDeleteTeamMember")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team from which the member will be removed |
 **userId** | **kotlin.String**| The team member to remove (User ID) |

### Return type

[**Team**](Team.md)

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

<a id="deleteDeleteTeamProject"></a>
# **deleteDeleteTeamProject**
> Team deleteDeleteTeamProject(id, projectId)

Revokes access for a project from a team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team whose permission will be revoked
val projectId : kotlin.String = projectId_example // kotlin.String | The project to remove (Project ID)
try {
    val result : Team = apiInstance.deleteDeleteTeamProject(id, projectId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#deleteDeleteTeamProject")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#deleteDeleteTeamProject")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team whose permission will be revoked |
 **projectId** | **kotlin.String**| The project to remove (Project ID) |

### Return type

[**Team**](Team.md)

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

<a id="getGetTeam"></a>
# **getGetTeam**
> Team getGetTeam(id)

Returns one team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | 
try {
    val result : Team = apiInstance.getGetTeam(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#getGetTeam")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#getGetTeam")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**|  |

### Return type

[**Team**](Team.md)

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

<a id="getGetTeams"></a>
# **getGetTeams**
> kotlin.collections.List&lt;TeamCollection&gt; getGetTeams()

Fetch all existing teams (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
try {
    val result : kotlin.collections.List<TeamCollection> = apiInstance.getGetTeams()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#getGetTeams")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#getGetTeams")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;TeamCollection&gt;**](TeamCollection.md)

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

<a id="patchPatchTeam"></a>
# **patchPatchTeam**
> Team patchPatchTeam(id, teamEditForm)

Update an existing team

Update an existing team, you can pass all or just a subset of all attributes (passing members will replace all existing ones)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | Team ID to update
val teamEditForm : TeamEditForm =  // TeamEditForm | 
try {
    val result : Team = apiInstance.patchPatchTeam(id, teamEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#patchPatchTeam")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#patchPatchTeam")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Team ID to update |
 **teamEditForm** | [**TeamEditForm**](TeamEditForm.md)|  |

### Return type

[**Team**](Team.md)

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

<a id="postPostTeam"></a>
# **postPostTeam**
> Team postPostTeam(teamEditForm)

Creates a new team

Creates a new team and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val teamEditForm : TeamEditForm =  // TeamEditForm | 
try {
    val result : Team = apiInstance.postPostTeam(teamEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#postPostTeam")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#postPostTeam")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **teamEditForm** | [**TeamEditForm**](TeamEditForm.md)|  |

### Return type

[**Team**](Team.md)

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

<a id="postPostTeamActivity"></a>
# **postPostTeamActivity**
> Team postPostTeamActivity(id, activityId)

Grant the team access to an activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team that is granted access
val activityId : kotlin.String = activityId_example // kotlin.String | The activity to grant acecess to (Activity ID)
try {
    val result : Team = apiInstance.postPostTeamActivity(id, activityId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#postPostTeamActivity")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#postPostTeamActivity")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team that is granted access |
 **activityId** | **kotlin.String**| The activity to grant acecess to (Activity ID) |

### Return type

[**Team**](Team.md)

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

<a id="postPostTeamCustomer"></a>
# **postPostTeamCustomer**
> Team postPostTeamCustomer(id, customerId)

Grant the team access to a customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team that is granted access
val customerId : kotlin.String = customerId_example // kotlin.String | The customer to grant acecess to (Customer ID)
try {
    val result : Team = apiInstance.postPostTeamCustomer(id, customerId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#postPostTeamCustomer")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#postPostTeamCustomer")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team that is granted access |
 **customerId** | **kotlin.String**| The customer to grant acecess to (Customer ID) |

### Return type

[**Team**](Team.md)

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

<a id="postPostTeamMember"></a>
# **postPostTeamMember**
> Team postPostTeamMember(id, userId)

Add a new member to a team

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team which will receive the new member
val userId : kotlin.String = userId_example // kotlin.String | The team member to add (User ID)
try {
    val result : Team = apiInstance.postPostTeamMember(id, userId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#postPostTeamMember")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#postPostTeamMember")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team which will receive the new member |
 **userId** | **kotlin.String**| The team member to add (User ID) |

### Return type

[**Team**](Team.md)

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

<a id="postPostTeamProject"></a>
# **postPostTeamProject**
> Team postPostTeamProject(id, projectId)

Grant the team access to a project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TeamApi()
val id : kotlin.String = id_example // kotlin.String | The team that is granted access
val projectId : kotlin.String = projectId_example // kotlin.String | The project to grant acecess to (Project ID)
try {
    val result : Team = apiInstance.postPostTeamProject(id, projectId)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TeamApi#postPostTeamProject")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TeamApi#postPostTeamProject")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The team that is granted access |
 **projectId** | **kotlin.String**| The project to grant acecess to (Project ID) |

### Return type

[**Team**](Team.md)

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

