# ActivityApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteActivityRate**](ActivityApi.md#deleteDeleteActivityRate) | **DELETE** /api/activities/{id}/rates/{rateId} | Deletes one rate for an activity
[**getGetActivities**](ActivityApi.md#getGetActivities) | **GET** /api/activities | Returns a collection of activities (which are visible to the user)
[**getGetActivity**](ActivityApi.md#getGetActivity) | **GET** /api/activities/{id} | Returns one activity
[**getGetActivityRates**](ActivityApi.md#getGetActivityRates) | **GET** /api/activities/{id}/rates | Returns a collection of all rates for one activity
[**patchAppApiActivityMeta**](ActivityApi.md#patchAppApiActivityMeta) | **PATCH** /api/activities/{id}/meta | Sets the value of a meta-field for an existing activity
[**patchPatchActivity**](ActivityApi.md#patchPatchActivity) | **PATCH** /api/activities/{id} | Update an existing activity
[**postPostActivity**](ActivityApi.md#postPostActivity) | **POST** /api/activities | Creates a new activity
[**postPostActivityRate**](ActivityApi.md#postPostActivityRate) | **POST** /api/activities/{id}/rates | Adds a new rate to an activity


<a id="deleteDeleteActivityRate"></a>
# **deleteDeleteActivityRate**
> deleteDeleteActivityRate(id, rateId)

Deletes one rate for an activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | The activity whose rate will be removed
val rateId : kotlin.String = rateId_example // kotlin.String | The rate to remove
try {
    apiInstance.deleteDeleteActivityRate(id, rateId)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#deleteDeleteActivityRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#deleteDeleteActivityRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The activity whose rate will be removed |
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

<a id="getGetActivities"></a>
# **getGetActivities**
> kotlin.collections.List&lt;ActivityCollection&gt; getGetActivities(project, projects, visible, globals, orderBy, order, term)

Returns a collection of activities (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val project : kotlin.String = project_example // kotlin.String | Project ID to filter activities
val projects : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of project IDs to filter activities, e.g.: projects[]=1&projects[]=2
val visible : kotlin.String = visible_example // kotlin.String | Visibility status to filter activities: 1=visible, 2=hidden, 3=all
val globals : kotlin.String = globals_example // kotlin.String | Use if you want to fetch only global activities. Allowed values: true (default: false)
val orderBy : kotlin.String = orderBy_example // kotlin.String | The field by which results will be ordered. Allowed values: id, name, project (default: name)
val order : kotlin.String = order_example // kotlin.String | The result order. Allowed values: ASC, DESC (default: ASC)
val term : kotlin.String = term_example // kotlin.String | Free search term
try {
    val result : kotlin.collections.List<ActivityCollection> = apiInstance.getGetActivities(project, projects, visible, globals, orderBy, order, term)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#getGetActivities")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#getGetActivities")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **project** | **kotlin.String**| Project ID to filter activities | [optional]
 **projects** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of project IDs to filter activities, e.g.: projects[]&#x3D;1&amp;projects[]&#x3D;2 | [optional] [default to arrayListOf()]
 **visible** | **kotlin.String**| Visibility status to filter activities: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;all | [optional] [default to &quot;1&quot;]
 **globals** | **kotlin.String**| Use if you want to fetch only global activities. Allowed values: true (default: false) | [optional]
 **orderBy** | **kotlin.String**| The field by which results will be ordered. Allowed values: id, name, project (default: name) | [optional]
 **order** | **kotlin.String**| The result order. Allowed values: ASC, DESC (default: ASC) | [optional]
 **term** | **kotlin.String**| Free search term | [optional]

### Return type

[**kotlin.collections.List&lt;ActivityCollection&gt;**](ActivityCollection.md)

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

<a id="getGetActivity"></a>
# **getGetActivity**
> ActivityEntity getGetActivity(id)

Returns one activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | Activity ID to fetch
try {
    val result : ActivityEntity = apiInstance.getGetActivity(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#getGetActivity")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#getGetActivity")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Activity ID to fetch |

### Return type

[**ActivityEntity**](ActivityEntity.md)

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

<a id="getGetActivityRates"></a>
# **getGetActivityRates**
> kotlin.collections.List&lt;ActivityRate&gt; getGetActivityRates(id)

Returns a collection of all rates for one activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | The activity whose rates will be returned
try {
    val result : kotlin.collections.List<ActivityRate> = apiInstance.getGetActivityRates(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#getGetActivityRates")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#getGetActivityRates")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The activity whose rates will be returned |

### Return type

[**kotlin.collections.List&lt;ActivityRate&gt;**](ActivityRate.md)

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

<a id="patchAppApiActivityMeta"></a>
# **patchAppApiActivityMeta**
> ActivityEntity patchAppApiActivityMeta(id, patchAppApiActivityMetaRequest)

Sets the value of a meta-field for an existing activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | Activity record ID to set the meta-field value for
val patchAppApiActivityMetaRequest : PatchAppApiActivityMetaRequest =  // PatchAppApiActivityMetaRequest | 
try {
    val result : ActivityEntity = apiInstance.patchAppApiActivityMeta(id, patchAppApiActivityMetaRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#patchAppApiActivityMeta")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#patchAppApiActivityMeta")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Activity record ID to set the meta-field value for |
 **patchAppApiActivityMetaRequest** | [**PatchAppApiActivityMetaRequest**](PatchAppApiActivityMetaRequest.md)|  | [optional]

### Return type

[**ActivityEntity**](ActivityEntity.md)

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

<a id="patchPatchActivity"></a>
# **patchPatchActivity**
> ActivityEntity patchPatchActivity(id, activityEditForm)

Update an existing activity

Update an existing activity, you can pass all or just a subset of all attributes

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | Activity ID to update
val activityEditForm : ActivityEditForm =  // ActivityEditForm | 
try {
    val result : ActivityEntity = apiInstance.patchPatchActivity(id, activityEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#patchPatchActivity")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#patchPatchActivity")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Activity ID to update |
 **activityEditForm** | [**ActivityEditForm**](ActivityEditForm.md)|  |

### Return type

[**ActivityEntity**](ActivityEntity.md)

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

<a id="postPostActivity"></a>
# **postPostActivity**
> ActivityEntity postPostActivity(activityEditForm)

Creates a new activity

Creates a new activity and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val activityEditForm : ActivityEditForm =  // ActivityEditForm | 
try {
    val result : ActivityEntity = apiInstance.postPostActivity(activityEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#postPostActivity")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#postPostActivity")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **activityEditForm** | [**ActivityEditForm**](ActivityEditForm.md)|  |

### Return type

[**ActivityEntity**](ActivityEntity.md)

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

<a id="postPostActivityRate"></a>
# **postPostActivityRate**
> ActivityRate postPostActivityRate(id, activityRateForm)

Adds a new rate to an activity

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActivityApi()
val id : kotlin.String = id_example // kotlin.String | The activity to add the rate for
val activityRateForm : ActivityRateForm =  // ActivityRateForm | 
try {
    val result : ActivityRate = apiInstance.postPostActivityRate(id, activityRateForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActivityApi#postPostActivityRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActivityApi#postPostActivityRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The activity to add the rate for |
 **activityRateForm** | [**ActivityRateForm**](ActivityRateForm.md)|  |

### Return type

[**ActivityRate**](ActivityRate.md)

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

