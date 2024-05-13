# CustomerApi

All URIs are relative to *http://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**deleteDeleteCustomerRate**](CustomerApi.md#deleteDeleteCustomerRate) | **DELETE** /api/customers/{id}/rates/{rateId} | Deletes one rate for a customer
[**getGetCustomer**](CustomerApi.md#getGetCustomer) | **GET** /api/customers/{id} | Returns one customer
[**getGetCustomerRates**](CustomerApi.md#getGetCustomerRates) | **GET** /api/customers/{id}/rates | Returns a collection of all rates for one customer
[**getGetCustomers**](CustomerApi.md#getGetCustomers) | **GET** /api/customers | Returns a collection of customers (which are visible to the user)
[**patchAppApiCustomerMeta**](CustomerApi.md#patchAppApiCustomerMeta) | **PATCH** /api/customers/{id}/meta | Sets the value of a meta-field for an existing customer
[**patchPatchCustomer**](CustomerApi.md#patchPatchCustomer) | **PATCH** /api/customers/{id} | Update an existing customer
[**postPostCustomer**](CustomerApi.md#postPostCustomer) | **POST** /api/customers | Creates a new customer
[**postPostCustomerRate**](CustomerApi.md#postPostCustomerRate) | **POST** /api/customers/{id}/rates | Adds a new rate to a customer


<a id="deleteDeleteCustomerRate"></a>
# **deleteDeleteCustomerRate**
> deleteDeleteCustomerRate(id, rateId)

Deletes one rate for a customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | The customer whose rate will be removed
val rateId : kotlin.String = rateId_example // kotlin.String | The rate to remove
try {
    apiInstance.deleteDeleteCustomerRate(id, rateId)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#deleteDeleteCustomerRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#deleteDeleteCustomerRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The customer whose rate will be removed |
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

<a id="getGetCustomer"></a>
# **getGetCustomer**
> CustomerEntity getGetCustomer(id)

Returns one customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | 
try {
    val result : CustomerEntity = apiInstance.getGetCustomer(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#getGetCustomer")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#getGetCustomer")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**|  |

### Return type

[**CustomerEntity**](CustomerEntity.md)

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

<a id="getGetCustomerRates"></a>
# **getGetCustomerRates**
> kotlin.collections.List&lt;CustomerRate&gt; getGetCustomerRates(id)

Returns a collection of all rates for one customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | The customer whose rates will be returned
try {
    val result : kotlin.collections.List<CustomerRate> = apiInstance.getGetCustomerRates(id)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#getGetCustomerRates")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#getGetCustomerRates")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The customer whose rates will be returned |

### Return type

[**kotlin.collections.List&lt;CustomerRate&gt;**](CustomerRate.md)

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

<a id="getGetCustomers"></a>
# **getGetCustomers**
> kotlin.collections.List&lt;CustomerCollection&gt; getGetCustomers(visible, order, orderBy, term)

Returns a collection of customers (which are visible to the user)

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val visible : kotlin.String = visible_example // kotlin.String | Visibility status to filter customers: 1=visible, 2=hidden, 3=both
val order : kotlin.String = order_example // kotlin.String | The result order. Allowed values: ASC, DESC (default: ASC)
val orderBy : kotlin.String = orderBy_example // kotlin.String | The field by which results will be ordered. Allowed values: id, name (default: name)
val term : kotlin.String = term_example // kotlin.String | Free search term
try {
    val result : kotlin.collections.List<CustomerCollection> = apiInstance.getGetCustomers(visible, order, orderBy, term)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#getGetCustomers")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#getGetCustomers")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **visible** | **kotlin.String**| Visibility status to filter customers: 1&#x3D;visible, 2&#x3D;hidden, 3&#x3D;both | [optional] [default to &quot;1&quot;]
 **order** | **kotlin.String**| The result order. Allowed values: ASC, DESC (default: ASC) | [optional]
 **orderBy** | **kotlin.String**| The field by which results will be ordered. Allowed values: id, name (default: name) | [optional]
 **term** | **kotlin.String**| Free search term | [optional]

### Return type

[**kotlin.collections.List&lt;CustomerCollection&gt;**](CustomerCollection.md)

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

<a id="patchAppApiCustomerMeta"></a>
# **patchAppApiCustomerMeta**
> CustomerEntity patchAppApiCustomerMeta(id, patchAppApiActivityMetaRequest)

Sets the value of a meta-field for an existing customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | Customer record ID to set the meta-field value for
val patchAppApiActivityMetaRequest : PatchAppApiActivityMetaRequest =  // PatchAppApiActivityMetaRequest | 
try {
    val result : CustomerEntity = apiInstance.patchAppApiCustomerMeta(id, patchAppApiActivityMetaRequest)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#patchAppApiCustomerMeta")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#patchAppApiCustomerMeta")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Customer record ID to set the meta-field value for |
 **patchAppApiActivityMetaRequest** | [**PatchAppApiActivityMetaRequest**](PatchAppApiActivityMetaRequest.md)|  | [optional]

### Return type

[**CustomerEntity**](CustomerEntity.md)

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

<a id="patchPatchCustomer"></a>
# **patchPatchCustomer**
> CustomerEntity patchPatchCustomer(id, customerEditForm)

Update an existing customer

Update an existing customer, you can pass all or just a subset of all attributes

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | Customer ID to update
val customerEditForm : CustomerEditForm =  // CustomerEditForm | 
try {
    val result : CustomerEntity = apiInstance.patchPatchCustomer(id, customerEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#patchPatchCustomer")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#patchPatchCustomer")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| Customer ID to update |
 **customerEditForm** | [**CustomerEditForm**](CustomerEditForm.md)|  |

### Return type

[**CustomerEntity**](CustomerEntity.md)

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

<a id="postPostCustomer"></a>
# **postPostCustomer**
> CustomerEntity postPostCustomer(customerEditForm)

Creates a new customer

Creates a new customer and returns it afterwards

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val customerEditForm : CustomerEditForm =  // CustomerEditForm | 
try {
    val result : CustomerEntity = apiInstance.postPostCustomer(customerEditForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#postPostCustomer")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#postPostCustomer")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **customerEditForm** | [**CustomerEditForm**](CustomerEditForm.md)|  |

### Return type

[**CustomerEntity**](CustomerEntity.md)

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

<a id="postPostCustomerRate"></a>
# **postPostCustomerRate**
> CustomerRate postPostCustomerRate(id, customerRateForm)

Adds a new rate to a customer

### Example
```kotlin
// Import classes:
//import de.progeek.kimai.infrastructure.*
//import de.progeek.kimai.models.*

val apiInstance = CustomerApi()
val id : kotlin.String = id_example // kotlin.String | The customer to add the rate for
val customerRateForm : CustomerRateForm =  // CustomerRateForm | 
try {
    val result : CustomerRate = apiInstance.postPostCustomerRate(id, customerRateForm)
    println(result)
} catch (e: ClientException) {
    println("4xx response calling CustomerApi#postPostCustomerRate")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling CustomerApi#postPostCustomerRate")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **id** | **kotlin.String**| The customer to add the rate for |
 **customerRateForm** | [**CustomerRateForm**](CustomerRateForm.md)|  |

### Return type

[**CustomerRate**](CustomerRate.md)

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

