# TimesheetApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteTimesheet**](TimesheetApi.md#deleteDeleteTimesheet) | **DELETE** /api/timesheets/{id} | Delete an existing timesheet record
[**getActiveTimesheet**](TimesheetApi.md#getActiveTimesheet) | **GET** /api/timesheets/active | Returns the collection of active timesheet records
[**getGetTimesheet**](TimesheetApi.md#getGetTimesheet) | **GET** /api/timesheets/{id} | Returns one timesheet record
[**getGetTimesheets**](TimesheetApi.md#getGetTimesheets) | **GET** /api/timesheets | Returns a collection of timesheet records (which are visible to the user)
[**getRecentTimesheet**](TimesheetApi.md#getRecentTimesheet) | **GET** /api/timesheets/recent | Returns the collection of recent user activities
[**getRestartTimesheetGet**](TimesheetApi.md#getRestartTimesheetGet) | **GET** /api/timesheets/{id}/restart | Restarts a previously stopped timesheet record for the current user
[**getStopTimesheetGet**](TimesheetApi.md#getStopTimesheetGet) | **GET** /api/timesheets/{id}/stop | Stops an active timesheet record.
[**patchAppApiTimesheetMeta**](TimesheetApi.md#patchAppApiTimesheetMeta) | **PATCH** /api/timesheets/{id}/meta | Sets the value of a meta-field for an existing timesheet.
[**patchDuplicateTimesheet**](TimesheetApi.md#patchDuplicateTimesheet) | **PATCH** /api/timesheets/{id}/duplicate | Duplicates an existing timesheet record
[**patchExportTimesheet**](TimesheetApi.md#patchExportTimesheet) | **PATCH** /api/timesheets/{id}/export | Switch the export state of a timesheet record to (un-)lock it
[**patchPatchTimesheet**](TimesheetApi.md#patchPatchTimesheet) | **PATCH** /api/timesheets/{id} | Update an existing timesheet record
[**patchRestartTimesheet**](TimesheetApi.md#patchRestartTimesheet) | **PATCH** /api/timesheets/{id}/restart | Restarts a previously stopped timesheet record for the current user
[**patchStopTimesheet**](TimesheetApi.md#patchStopTimesheet) | **PATCH** /api/timesheets/{id}/stop | Stops an active timesheet record.
[**postPostTimesheet**](TimesheetApi.md#postPostTimesheet) | **POST** /api/timesheets | Creates a new timesheet record


<a id="deleteDeleteTimesheet"></a>
# **deleteDeleteTimesheet**
> deleteDeleteTimesheet(id)

Delete an existing timesheet record

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to delete
try {
    apiInstance.deleteDeleteTimesheet(id)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#deleteDeleteTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#deleteDeleteTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to delete |

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

<a id="getActiveTimesheet"></a>
# **getActiveTimesheet**
> kotlin.collections.List&lt;TimesheetCollectionExpanded&gt; getActiveTimesheet()

Returns the collection of active timesheet records

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
try {
    val result : kotlin.collections.List<TimesheetCollectionExpanded> = apiInstance.getActiveTimesheet()
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getActiveTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getActiveTimesheet")
    e.printStackTrace()
}
```

### Parameters
This endpoint does not need any parameter.

### Return type

[**kotlin.collections.List&lt;TimesheetCollectionExpanded&gt;**](TimesheetCollectionExpanded.md)

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

<a id="getGetTimesheet"></a>
# **getGetTimesheet**
> TimesheetEntity getGetTimesheet(id)

Returns one timesheet record

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to fetch
try {
    val result : TimesheetEntity = apiInstance.getGetTimesheet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getGetTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getGetTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to fetch |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="getGetTimesheets"></a>
# **getGetTimesheets**
> kotlin.collections.List&lt;TimesheetCollection&gt; getGetTimesheets(user, users, customer, customers, project, projects, activity, activities, page, size, tags, orderBy, order, begin, end, exported, active, billable, full, term, modifiedAfter)

Returns a collection of timesheet records (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val user : kotlin.String = user_example // kotlin.String | User ID to filter timesheets. Needs permission 'view_other_timesheet', pass 'all' to fetch data for all user (default: current user)
val users : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of user IDs to filter, e.g.: users[]=1&users[]=2 (ignored if user=all)
val customer : kotlin.String = customer_example // kotlin.String | Customer ID to filter timesheets
val customers : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of customer IDs to filter, e.g.: customers[]=1&customers[]=2
val project : kotlin.String = project_example // kotlin.String | Project ID to filter timesheets
val projects : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of project IDs to filter, e.g.: projects[]=1&projects[]=2
val activity : kotlin.String = activity_example // kotlin.String | Activity ID to filter timesheets
val activities : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of activity IDs to filter, e.g.: activities[]=1&activities[]=2
val page : kotlin.String = page_example // kotlin.String | The page to display, renders a 404 if not found (default: 1)
val size : kotlin.String = size_example // kotlin.String | The amount of entries for each page (default: 50)
val tags : kotlin.collections.List<kotlin.Any> =  // kotlin.collections.List<kotlin.Any> | List of tag names, e.g. tags[]=bar&tags[]=foo
val orderBy : kotlin.String = orderBy_example // kotlin.String | The field by which results will be ordered. Allowed values: id, begin, end, rate (default: begin)
val order : kotlin.String = order_example // kotlin.String | The result order. Allowed values: ASC, DESC (default: DESC)
val begin : kotlin.String = begin_example // kotlin.String | Only records after this date will be included (format: HTML5)
val end : kotlin.String = end_example // kotlin.String | Only records before this date will be included (format: HTML5)
val exported : kotlin.String = exported_example // kotlin.String | Use this flag if you want to filter for export state. Allowed values: 0=not exported, 1=exported (default: all)
val active : kotlin.String = active_example // kotlin.String | Filter for running/active records. Allowed values: 0=stopped, 1=active (default: all)
val billable : kotlin.String = billable_example // kotlin.String | Filter for non-/billable records. Allowed values: 0=non-billable, 1=billable (default: all)
val full : kotlin.String = full_example // kotlin.String | Allows to fetch fully serialized objects including subresources. Allowed values: true (default: false)
val term : kotlin.String = term_example // kotlin.String | Free search term
val modifiedAfter : kotlin.String = modifiedAfter_example // kotlin.String | Only records changed after this date will be included (format: HTML5). Available since Kimai 1.10 and works only for records that were created/updated since then.
try {
    val result : kotlin.collections.List<TimesheetCollection> = apiInstance.getGetTimesheets(user, users, customer, customers, project, projects, activity, activities, page, size, tags, orderBy, order, begin, end, exported, active, billable, full, term, modifiedAfter)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getGetTimesheets")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getGetTimesheets")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **user** | **kotlin.String**| User ID to filter timesheets. Needs permission &#39;view_other_timesheet&#39;, pass &#39;all&#39; to fetch data for all user (default: current user) | [optional]
 **users** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of user IDs to filter, e.g.: users[]&#x3D;1&amp;users[]&#x3D;2 (ignored if user&#x3D;all) | [optional] [default to arrayListOf()]
 **customer** | **kotlin.String**| Customer ID to filter timesheets | [optional]
 **customers** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of customer IDs to filter, e.g.: customers[]&#x3D;1&amp;customers[]&#x3D;2 | [optional] [default to arrayListOf()]
 **project** | **kotlin.String**| Project ID to filter timesheets | [optional]
 **projects** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of project IDs to filter, e.g.: projects[]&#x3D;1&amp;projects[]&#x3D;2 | [optional] [default to arrayListOf()]
 **activity** | **kotlin.String**| Activity ID to filter timesheets | [optional]
 **activities** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of activity IDs to filter, e.g.: activities[]&#x3D;1&amp;activities[]&#x3D;2 | [optional] [default to arrayListOf()]
 **page** | **kotlin.String**| The page to display, renders a 404 if not found (default: 1) | [optional]
 **size** | **kotlin.String**| The amount of entries for each page (default: 50) | [optional]
 **tags** | [**kotlin.collections.List&lt;kotlin.Any&gt;**](kotlin.Any.md)| List of tag names, e.g. tags[]&#x3D;bar&amp;tags[]&#x3D;foo | [optional] [default to arrayListOf()]
 **orderBy** | **kotlin.String**| The field by which results will be ordered. Allowed values: id, begin, end, rate (default: begin) | [optional]
 **order** | **kotlin.String**| The result order. Allowed values: ASC, DESC (default: DESC) | [optional]
 **begin** | **kotlin.String**| Only records after this date will be included (format: HTML5) | [optional]
 **end** | **kotlin.String**| Only records before this date will be included (format: HTML5) | [optional]
 **exported** | **kotlin.String**| Use this flag if you want to filter for export state. Allowed values: 0&#x3D;not exported, 1&#x3D;exported (default: all) | [optional]
 **active** | **kotlin.String**| Filter for running/active records. Allowed values: 0&#x3D;stopped, 1&#x3D;active (default: all) | [optional]
 **billable** | **kotlin.String**| Filter for non-/billable records. Allowed values: 0&#x3D;non-billable, 1&#x3D;billable (default: all) | [optional]
 **full** | **kotlin.String**| Allows to fetch fully serialized objects including subresources. Allowed values: true (default: false) | [optional]
 **term** | **kotlin.String**| Free search term | [optional]
 **modifiedAfter** | **kotlin.String**| Only records changed after this date will be included (format: HTML5). Available since Kimai 1.10 and works only for records that were created/updated since then. | [optional]

### Return type

[**kotlin.collections.List&lt;TimesheetCollection&gt;**](TimesheetCollection.md)

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

<a id="getRecentTimesheet"></a>
# **getRecentTimesheet**
> kotlin.collections.List&lt;TimesheetCollectionExpanded&gt; getRecentTimesheet(begin, size)

Returns the collection of recent user activities

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val begin : kotlin.String = begin_example // kotlin.String | Only records after this date will be included. Default: today - 1 year (format: HTML5)
val size : kotlin.String = size_example // kotlin.String | The amount of entries (default: 10)
try {
    val result : kotlin.collections.List<TimesheetCollectionExpanded> = apiInstance.getRecentTimesheet(begin, size)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getRecentTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getRecentTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **begin** | **kotlin.String**| Only records after this date will be included. Default: today - 1 year (format: HTML5) | [optional]
 **size** | **kotlin.String**| The amount of entries (default: 10) | [optional]

### Return type

[**kotlin.collections.List&lt;TimesheetCollectionExpanded&gt;**](TimesheetCollectionExpanded.md)

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

<a id="getRestartTimesheetGet"></a>
# **getRestartTimesheetGet**
> TimesheetEntity getRestartTimesheetGet(id, getRestartTimesheetGetRequest)

Restarts a previously stopped timesheet record for the current user

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to restart
val getRestartTimesheetGetRequest : GetRestartTimesheetGetRequest =  // GetRestartTimesheetGetRequest | 
try {
    val result : TimesheetEntity = apiInstance.getRestartTimesheetGet(id, getRestartTimesheetGetRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getRestartTimesheetGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getRestartTimesheetGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to restart |
 **getRestartTimesheetGetRequest** | [**GetRestartTimesheetGetRequest**](GetRestartTimesheetGetRequest.md)|  | [optional]

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="getStopTimesheetGet"></a>
# **getStopTimesheetGet**
> TimesheetEntity getStopTimesheetGet(id)

Stops an active timesheet record.

This route is available via GET and PATCH, as users over and over again run into errors when stopping. Likely caused by a slow JS engine and a fast-click after page reload.

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to stop
try {
    val result : TimesheetEntity = apiInstance.getStopTimesheetGet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#getStopTimesheetGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#getStopTimesheetGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to stop |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchAppApiTimesheetMeta"></a>
# **patchAppApiTimesheetMeta**
> TimesheetEntity patchAppApiTimesheetMeta(id, patchAppApiActivityMetaRequest)

Sets the value of a meta-field for an existing timesheet.

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to set the meta-field value for
val patchAppApiActivityMetaRequest : PatchAppApiActivityMetaRequest =  // PatchAppApiActivityMetaRequest | 
try {
    val result : TimesheetEntity = apiInstance.patchAppApiTimesheetMeta(id, patchAppApiActivityMetaRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchAppApiTimesheetMeta")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchAppApiTimesheetMeta")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to set the meta-field value for |
 **patchAppApiActivityMetaRequest** | [**PatchAppApiActivityMetaRequest**](PatchAppApiActivityMetaRequest.md)|  | [optional]

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchDuplicateTimesheet"></a>
# **patchDuplicateTimesheet**
> TimesheetEntity patchDuplicateTimesheet(id)

Duplicates an existing timesheet record

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to duplicate
try {
    val result : TimesheetEntity = apiInstance.patchDuplicateTimesheet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchDuplicateTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchDuplicateTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to duplicate |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchExportTimesheet"></a>
# **patchExportTimesheet**
> TimesheetEntity patchExportTimesheet(id)

Switch the export state of a timesheet record to (un-)lock it

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to switch export state
try {
    val result : TimesheetEntity = apiInstance.patchExportTimesheet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchExportTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchExportTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to switch export state |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchPatchTimesheet"></a>
# **patchPatchTimesheet**
> TimesheetEntity patchPatchTimesheet(id, timesheetEditForm)

Update an existing timesheet record

Update an existing timesheet record, you can pass all or just a subset of the attributes.

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to update
val timesheetEditForm : TimesheetEditForm =  // TimesheetEditForm | 
try {
    val result : TimesheetEntity = apiInstance.patchPatchTimesheet(id, timesheetEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchPatchTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchPatchTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to update |
 **timesheetEditForm** | [**TimesheetEditForm**](TimesheetEditForm.md)|  |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchRestartTimesheet"></a>
# **patchRestartTimesheet**
> TimesheetEntity patchRestartTimesheet(id, getRestartTimesheetGetRequest)

Restarts a previously stopped timesheet record for the current user

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to restart
val getRestartTimesheetGetRequest : GetRestartTimesheetGetRequest =  // GetRestartTimesheetGetRequest | 
try {
    val result : TimesheetEntity = apiInstance.patchRestartTimesheet(id, getRestartTimesheetGetRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchRestartTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchRestartTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to restart |
 **getRestartTimesheetGetRequest** | [**GetRestartTimesheetGetRequest**](GetRestartTimesheetGetRequest.md)|  | [optional]

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="patchStopTimesheet"></a>
# **patchStopTimesheet**
> TimesheetEntity patchStopTimesheet(id)

Stops an active timesheet record.

This route is available via GET and PATCH, as users over and over again run into errors when stopping. Likely caused by a slow JS engine and a fast-click after page reload.

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val id : kotlin.String = id_example // kotlin.String | Timesheet record ID to stop
try {
    val result : TimesheetEntity = apiInstance.patchStopTimesheet(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#patchStopTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#patchStopTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Timesheet record ID to stop |

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

<a id="postPostTimesheet"></a>
# **postPostTimesheet**
> TimesheetEntity postPostTimesheet(timesheetEditForm, full)

Creates a new timesheet record

Creates a new timesheet record for the current user and returns it afterwards.

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = TimesheetApi()
val timesheetEditForm : TimesheetEditForm =  // TimesheetEditForm | 
val full : kotlin.String = full_example // kotlin.String | Allows to fetch fully serialized objects including subresources (TimesheetExpanded). Allowed values: true (default: false)
try {
    val result : TimesheetEntity = apiInstance.postPostTimesheet(timesheetEditForm, full)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling TimesheetApi#postPostTimesheet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling TimesheetApi#postPostTimesheet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **timesheetEditForm** | [**TimesheetEditForm**](TimesheetEditForm.md)|  |
 **full** | **kotlin.String**| Allows to fetch fully serialized objects including subresources (TimesheetExpanded). Allowed values: true (default: false) | [optional]

### Return type

[**TimesheetEntity**](TimesheetEntity.md)

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

