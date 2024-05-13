# ProjectApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteProjectRate**](ProjectApi.md#deleteDeleteProjectRate) | **DELETE** /api/projects/{id}/rates/{rateId} | Deletes one rate for a project
[**getGetProject**](ProjectApi.md#getGetProject) | **GET** /api/projects/{id} | Returns one project
[**getGetProjectRates**](ProjectApi.md#getGetProjectRates) | **GET** /api/projects/{id}/rates | Returns a collection of all rates for one project
[**getGetProjects**](ProjectApi.md#getGetProjects) | **GET** /api/projects | Returns a collection of projects (which are visible to the user)
[**patchAppApiProjectMeta**](ProjectApi.md#patchAppApiProjectMeta) | **PATCH** /api/projects/{id}/meta | Sets the value of a meta-field for an existing project
[**patchPatchProject**](ProjectApi.md#patchPatchProject) | **PATCH** /api/projects/{id} | Update an existing project
[**postPostProject**](ProjectApi.md#postPostProject) | **POST** /api/projects | Creates a new project
[**postPostProjectRate**](ProjectApi.md#postPostProjectRate) | **POST** /api/projects/{id}/rates | Adds a new rate to a project


<a id="deleteDeleteProjectRate"></a>
# **deleteDeleteProjectRate**
> deleteDeleteProjectRate(id, rateId)

Deletes one rate for a project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | The project whose rate will be removed
val rateId : kotlin.String = rateId_example // kotlin.String | The rate to remove
try {
    apiInstance.deleteDeleteProjectRate(id, rateId)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#deleteDeleteProjectRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#deleteDeleteProjectRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The project whose rate will be removed |
 **rateId** | **kotlin.String**| The rate to remove |

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

<a id="getGetProject"></a>
# **getGetProject**
> ProjectEntity getGetProject(id)

Returns one project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | 
try {
    val result : ProjectEntity = apiInstance.getGetProject(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#getGetProject")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#getGetProject")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**|  |

### Return type

[**ProjectEntity**](ProjectEntity.md)

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

<a id="getGetProjectRates"></a>
# **getGetProjectRates**
> kotlin.collections.List&lt;ProjectRate&gt; getGetProjectRates(id)

Returns a collection of all rates for one project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | The project whose rates will be returned
try {
    val result : kotlin.collections.List<ProjectRate> = apiInstance.getGetProjectRates(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#getGetProjectRates")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#getGetProjectRates")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The project whose rates will be returned |

### Return type

[**kotlin.collections.List&lt;ProjectRate&gt;**](ProjectRate.md)

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

<a id="getGetProjects"></a>
# **getGetProjects**
> kotlin.collections.List&lt;ProjectCollection&gt; getGetProjects(customer, customers, visible, start, end, ignoreDates, globalActivities, order, orderBy, term)

Returns a collection of projects (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val customer : kotlin.String = customer_example // kotlin.String | Customer ID to filter projects
val customers : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of customer IDs to filter, e.g.: customers[]=1&customers[]=2
val visible : kotlin.String = visible_example // kotlin.String | Visibility status to filter projects: 1=visible, 2=hidden, 3=both
val start : kotlin.String = start_example // kotlin.String | Only projects that started before this date will be included. Allowed format: HTML5 (default: now, if end is also empty)
val end : kotlin.String = end_example // kotlin.String | Only projects that ended after this date will be included. Allowed format: HTML5 (default: now, if start is also empty)
val ignoreDates : kotlin.String = ignoreDates_example // kotlin.String | If set, start and end are completely ignored. Allowed values: 1 (default: off)
val globalActivities : kotlin.String = globalActivities_example // kotlin.String | If given, filters projects by their 'global activity' support. Allowed values: 1 (supports global activities) and 0 (without global activities) (default: all)
val order : kotlin.String = order_example // kotlin.String | The result order. Allowed values: ASC, DESC (default: ASC)
val orderBy : kotlin.String = orderBy_example // kotlin.String | The field by which results will be ordered. Allowed values: id, name, customer (default: name)
val term : kotlin.String = term_example // kotlin.String | Free search term
try {
    val result : kotlin.collections.List<ProjectCollection> = apiInstance.getGetProjects(customer, customers, visible, start, end, ignoreDates, globalActivities, order, orderBy, term)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#getGetProjects")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#getGetProjects")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **customer** | **kotlin.String**| Customer ID to filter projects | [optional]
 **customers** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of customer IDs to filter, e.g.: customers[]&#x3D;1&amp;customers[]&#x3D;2 | [optional] [default to arrayListOf()]
 **visible** | **kotlin.String**| Visibility status to filter projects: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;both | [optional] [default to &quot;1&quot;]
 **start** | **kotlin.String**| Only projects that started before this date will be included. Allowed format: HTML5 (default: now, if end is also empty) | [optional]
 **end** | **kotlin.String**| Only projects that ended after this date will be included. Allowed format: HTML5 (default: now, if start is also empty) | [optional]
 **ignoreDates** | **kotlin.String**| If set, start and end are completely ignored. Allowed values: 1 (default: off) | [optional]
 **globalActivities** | **kotlin.String**| If given, filters projects by their &#39;global activity&#39; support. Allowed values: 1 (supports global activities) and 0 (without global activities) (default: all) | [optional]
 **order** | **kotlin.String**| The result order. Allowed values: ASC, DESC (default: ASC) | [optional]
 **orderBy** | **kotlin.String**| The field by which results will be ordered. Allowed values: id, name, customer (default: name) | [optional]
 **term** | **kotlin.String**| Free search term | [optional]

### Return type

[**kotlin.collections.List&lt;ProjectCollection&gt;**](ProjectCollection.md)

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

<a id="patchAppApiProjectMeta"></a>
# **patchAppApiProjectMeta**
> ProjectEntity patchAppApiProjectMeta(id, patchAppApiActivityMetaRequest)

Sets the value of a meta-field for an existing project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | Project record ID to set the meta-field value for
val patchAppApiActivityMetaRequest : PatchAppApiActivityMetaRequest =  // PatchAppApiActivityMetaRequest | 
try {
    val result : ProjectEntity = apiInstance.patchAppApiProjectMeta(id, patchAppApiActivityMetaRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#patchAppApiProjectMeta")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#patchAppApiProjectMeta")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Project record ID to set the meta-field value for |
 **patchAppApiActivityMetaRequest** | [**PatchAppApiActivityMetaRequest**](PatchAppApiActivityMetaRequest.md)|  | [optional]

### Return type

[**ProjectEntity**](ProjectEntity.md)

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

<a id="patchPatchProject"></a>
# **patchPatchProject**
> ProjectEntity patchPatchProject(id, projectEditForm)

Update an existing project

Update an existing project, you can pass all or just a subset of all attributes

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | Project ID to update
val projectEditForm : ProjectEditForm =  // ProjectEditForm | 
try {
    val result : ProjectEntity = apiInstance.patchPatchProject(id, projectEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#patchPatchProject")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#patchPatchProject")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Project ID to update |
 **projectEditForm** | [**ProjectEditForm**](ProjectEditForm.md)|  |

### Return type

[**ProjectEntity**](ProjectEntity.md)

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

<a id="postPostProject"></a>
# **postPostProject**
> ProjectEntity postPostProject(projectEditForm)

Creates a new project

Creates a new project and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val projectEditForm : ProjectEditForm =  // ProjectEditForm | 
try {
    val result : ProjectEntity = apiInstance.postPostProject(projectEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#postPostProject")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#postPostProject")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **projectEditForm** | [**ProjectEditForm**](ProjectEditForm.md)|  |

### Return type

[**ProjectEntity**](ProjectEntity.md)

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

<a id="postPostProjectRate"></a>
# **postPostProjectRate**
> ProjectRate postPostProjectRate(id, projectRateForm)

Adds a new rate to a project

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ProjectApi()
val id : kotlin.String = id_example // kotlin.String | The project to add the rate for
val projectRateForm : ProjectRateForm =  // ProjectRateForm | 
try {
    val result : ProjectRate = apiInstance.postPostProjectRate(id, projectRateForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ProjectApi#postPostProjectRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ProjectApi#postPostProjectRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The project to add the rate for |
 **projectRateForm** | [**ProjectRateForm**](ProjectRateForm.md)|  |

### Return type

[**ProjectRate**](ProjectRate.md)

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

