# ActionsApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getGetActivityActions**](ActionsApi.md#getGetActivityActions) | **GET** /api/actions/activity/{id}/{view}/{locale} | Get all item actions for the given Activity [for internal use]
[**getGetCustomerActions**](ActionsApi.md#getGetCustomerActions) | **GET** /api/actions/customer/{id}/{view}/{locale} | Get all item actions for the given Customer [for internal use]
[**getGetProjectActions**](ActionsApi.md#getGetProjectActions) | **GET** /api/actions/project/{id}/{view}/{locale} | Get all item actions for the given Project [for internal use]
[**getGetTimesheetActions**](ActionsApi.md#getGetTimesheetActions) | **GET** /api/actions/timesheet/{id}/{view}/{locale} | Get all item actions for the given Timesheet [for internal use]


<a id="getGetActivityActions"></a>
# **getGetActivityActions**
> PageAction getGetActivityActions(id, view, locale)

Get all item actions for the given Activity [for internal use]

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActionsApi()
val id : kotlin.String = id_example // kotlin.String | Activity ID to fetch
val view : kotlin.String = view_example // kotlin.String | View to display the actions at (e.g. index, custom)
val locale : kotlin.String = locale_example // kotlin.String | Language to translate the action title to (e.g. de, en)
try {
    val result : PageAction = apiInstance.getGetActivityActions(id, view, locale)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActionsApi#getGetActivityActions")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActionsApi#getGetActivityActions")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Activity ID to fetch |
 **view** | **kotlin.String**| View to display the actions at (e.g. index, custom) |
 **locale** | **kotlin.String**| Language to translate the action title to (e.g. de, en) |

### Return type

[**PageAction**](PageAction.md)

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

<a id="getGetCustomerActions"></a>
# **getGetCustomerActions**
> PageAction getGetCustomerActions(id, view, locale)

Get all item actions for the given Customer [for internal use]

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActionsApi()
val id : kotlin.String = id_example // kotlin.String | Customer ID to fetch
val view : kotlin.String = view_example // kotlin.String | View to display the actions at (e.g. index, custom)
val locale : kotlin.String = locale_example // kotlin.String | Language to translate the action title to (e.g. de, en)
try {
    val result : PageAction = apiInstance.getGetCustomerActions(id, view, locale)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActionsApi#getGetCustomerActions")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActionsApi#getGetCustomerActions")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Customer ID to fetch |
 **view** | **kotlin.String**| View to display the actions at (e.g. index, custom) |
 **locale** | **kotlin.String**| Language to translate the action title to (e.g. de, en) |

### Return type

[**PageAction**](PageAction.md)

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

<a id="getGetProjectActions"></a>
# **getGetProjectActions**
> PageAction getGetProjectActions(id, view, locale)

Get all item actions for the given Project [for internal use]

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActionsApi()
val id : kotlin.String = id_example // kotlin.String | Project ID to fetch
val view : kotlin.String = view_example // kotlin.String | View to display the actions at (e.g. index, custom)
val locale : kotlin.String = locale_example // kotlin.String | Language to translate the action title to (e.g. de, en)
try {
    val result : PageAction = apiInstance.getGetProjectActions(id, view, locale)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActionsApi#getGetProjectActions")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActionsApi#getGetProjectActions")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Project ID to fetch |
 **view** | **kotlin.String**| View to display the actions at (e.g. index, custom) |
 **locale** | **kotlin.String**| Language to translate the action title to (e.g. de, en) |

### Return type

[**PageAction**](PageAction.md)

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

<a id="getGetTimesheetActions"></a>
# **getGetTimesheetActions**
> PageAction getGetTimesheetActions(id, view, locale)

Get all item actions for the given Timesheet [for internal use]

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = ActionsApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet ID to fetch
val view : kotlin.String = view_example // kotlin.String | View to display the actions at (e.g. index, custom)
val locale : kotlin.String = locale_example // kotlin.String | Language to translate the action title to (e.g. de, en)
try {
    val result : PageAction = apiInstance.getGetTimesheetActions(id, view, locale)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling ActionsApi#getGetTimesheetActions")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ActionsApi#getGetTimesheetActions")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet ID to fetch |
 **view** | **kotlin.String**| View to display the actions at (e.g. index, custom) |
 **locale** | **kotlin.String**| Language to translate the action title to (e.g. de, en) |

### Return type

[**PageAction**](PageAction.md)

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

