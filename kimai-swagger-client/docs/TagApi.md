# TagApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteTag**](TagApi.md#deleteDeleteTag) | **DELETE** /api/tags/{id} | Delete a tag
[**getGetTags**](TagApi.md#getGetTags) | **GET** /api/tags | Fetch all existing tags
[**postPostTag**](TagApi.md#postPostTag) | **POST** /api/tags | Creates a new tag


<a id="deleteDeleteTag"></a>
# **deleteDeleteTag**
> deleteDeleteTag(id)

Delete a tag

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TagApi()
val id : kotlin.String = id_example // kotlin.String | Tag ID to delete
try {
    apiInstance.deleteDeleteTag(id)
} catch (e: ClientException) {
    println("4xx response calling TagApi#deleteDeleteTag")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TagApi#deleteDeleteTag")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Tag ID to delete |

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

<a id="getGetTags"></a>
# **getGetTags**
> kotlin.collections.List&lt;kotlin.String&gt; getGetTags(name)

Fetch all existing tags

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TagApi()
val name : kotlin.String = name_example // kotlin.String | Search term to filter tag list
try {
    val result : kotlin.collections.List<kotlin.String> = apiInstance.getGetTags(name)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TagApi#getGetTags")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TagApi#getGetTags")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **name** | **kotlin.String**| Search term to filter tag list | [optional]

### Return type

**kotlin.collections.List&lt;kotlin.String&gt;**

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

<a id="postPostTag"></a>
# **postPostTag**
> TagEntity postPostTag(tagEditForm)

Creates a new tag

Creates a new tag and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TagApi()
val tagEditForm : TagEditForm =  // TagEditForm | 
try {
    val result : TagEntity = apiInstance.postPostTag(tagEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TagApi#postPostTag")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TagApi#postPostTag")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **tagEditForm** | [**TagEditForm**](TagEditForm.md)|  |

### Return type

[**TagEntity**](TagEntity.md)

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

