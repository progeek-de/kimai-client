# DefaultApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**getAppApiConfigurationTimesheetconfig**](DefaultApi.md#getAppApiConfigurationTimesheetconfig) | **GET** /api/config/timesheet | Returns the timesheet configuration
[**getAppApiStatusPing**](DefaultApi.md#getAppApiStatusPing) | **GET** /api/ping | A testing route for the API
[**getAppApiStatusPlugin**](DefaultApi.md#getAppApiStatusPlugin) | **GET** /api/plugins | Returns information about installed Plugins
[**getAppApiStatusVersion**](DefaultApi.md#getAppApiStatusVersion) | **GET** /api/version | Returns information about the Kimai release


<a id="getAppApiConfigurationTimesheetconfig"></a>
# **getAppApiConfigurationTimesheetconfig**
> TimesheetConfig getAppApiConfigurationTimesheetconfig()

Returns the timesheet configuration

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = DefaultApi()
try {
    val result : TimesheetConfig = apiInstance.getAppApiConfigurationTimesheetconfig()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getAppApiConfigurationTimesheetconfig")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getAppApiConfigurationTimesheetconfig")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**TimesheetConfig**](TimesheetConfig.md)

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

<a id="getAppApiStatusPing"></a>
# **getAppApiStatusPing**
> kotlin.Any getAppApiStatusPing()

A testing route for the API

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = DefaultApi()
try {
    val result : kotlin.Any = apiInstance.getAppApiStatusPing()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getAppApiStatusPing")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getAppApiStatusPing")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.Any**](kotlin.Any.md)

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

<a id="getAppApiStatusPlugin"></a>
# **getAppApiStatusPlugin**
> kotlin.collections.List&lt;Plugin&gt; getAppApiStatusPlugin()

Returns information about installed Plugins

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = DefaultApi()
try {
    val result : kotlin.collections.List<Plugin> = apiInstance.getAppApiStatusPlugin()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getAppApiStatusPlugin")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getAppApiStatusPlugin")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;Plugin&gt;**](Plugin.md)

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

<a id="getAppApiStatusVersion"></a>
# **getAppApiStatusVersion**
> Version getAppApiStatusVersion()

Returns information about the Kimai release

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = DefaultApi()
try {
    val result : Version = apiInstance.getAppApiStatusVersion()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling DefaultApi#getAppApiStatusVersion")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling DefaultApi#getAppApiStatusVersion")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**Version**](Version.md)

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

