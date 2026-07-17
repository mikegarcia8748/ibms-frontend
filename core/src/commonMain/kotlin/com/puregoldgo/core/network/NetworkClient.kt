package com.puregoldgo.core.network

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@PublishedApi
internal const val TAG = "NetworkBoundResource"

@PublishedApi
internal val lenientJson = Json { ignoreUnknownKeys = true; isLenient = true }

// ─── Public API ───────────────────────────────────────────────────────────────

/**
 * Sends an HTTP request and maps the response into a [Resource].
 *
 * - If the API envelope `result` field is `"success"`, returns [Resource.Success].
 * - If the API envelope `result` field is `"failed"`, returns [Resource.Failed] with the
 *   deserialized body available via [Resource.Failed.data].
 * - If the HTTP status is 2xx without a recognized result field, returns [Resource.Success].
 * - Otherwise returns [Resource.Error] with a [DomainException.ApiException].
 * - Catches all exceptions and delegates to [handleExceptionOnce].
 */
suspend inline fun <reified T> HttpClient.sendRequest(
    tag: String? = null,
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
): Resource<T> {
    var httpResponse: String? = null
    var maskedHttpResponse: String = ""

    // Capture request details for logging
    val builder = HttpRequestBuilder().apply(requestBuilder)
    val endpoint = builder.url.buildString()
    val method = builder.method.value
    val requestPayload = PayloadMasker.mask(builder.body.toString())

    return try {
        val response: HttpResponse = request(requestBuilder)

        httpResponse = response.bodyAsText()
        maskedHttpResponse = PayloadMasker.mask(httpResponse) ?: ""

        val responseBody: T = response.body()
        val result = getResultValue(httpResponse)

        when {
            result?.lowercase() == HttpResult.Success.value -> Resource.Success(responseBody)
            result?.lowercase() == HttpResult.Failed.value -> {
                Napier.e(
                    message = "API Failed: $method $endpoint\nResponse: $httpResponse",
                    tag = TAG,
                )
                Resource.Failed(data = responseBody)
            }
            response.status.value in 200..299 -> Resource.Success(responseBody)
            else -> {
                val statusCode = response.status
                val exception = getHttpRequestException(statusCode)
                val errorType = getHttpErrorType(statusCode)

                Napier.e(
                    message = "HTTP Error: $statusCode $method $endpoint\nResponse: $httpResponse",
                    tag = TAG,
                )
                Resource.Error(
                    DomainException.ApiException(
                        code = statusCode.value,
                        message = "$errorType: ${exception.message ?: "HTTP Error"}",
                    ),
                )
            }
        }
    } catch (e: Exception) {
        val errorType = when (e) {
            is RedirectResponseException -> "RedirectionResponseError"
            is ClientRequestException -> "ClientRequestError"
            is ServerResponseException -> "ServerError"
            else -> "GeneralException"
        }
        val statusCode = if (e is ResponseException) e.response.status.value else 0

        Napier.e(
            throwable = e,
            message = "Network Exception: $errorType $method $endpoint\nStatus: $statusCode\nMessage: ${e.message}",
            tag = TAG,
        )
        handleExceptionOnce(e)
    }
}

/**
 * Sends a simple HTTP request and maps the response into a [Resource].
 * Unlike [sendRequest], this variant does not inspect the API envelope result field —
 * it is intended for external or non-envelope endpoints.
 */
suspend inline fun <reified T> HttpClient.sendExternalRequest(
    crossinline requestBuilder: HttpRequestBuilder.() -> Unit,
): Resource<T> {
    return try {
        val response: HttpResponse = request(requestBuilder)
        val responseBody: T = response.body()
        Resource.Success(responseBody)
    } catch (e: ResponseException) {
        Napier.e(throwable = e, message = e.message.orEmpty(), tag = TAG)
        Resource.Error(Throwable(ERROR_TECHNICAL_EXCEPTION))
    } catch (e: Exception) {
        Napier.e(throwable = e, message = e.message.orEmpty(), tag = TAG)
        Resource.Error(Throwable(ERROR_TECHNICAL_EXCEPTION))
    }
}

// ─── Exception Handling ───────────────────────────────────────────────────────

/**
 * Maps a caught exception to a [Resource.Error] with a user-friendly message.
 */
fun <T> handleExceptionOnce(e: Exception): Resource<T> {
    val errorMessage = when {
        e is RedirectResponseException -> ERROR_HTTP_REDIRECT_EXCEPTION
        e is ClientRequestException -> ERROR_HTTP_CLIENT_REQUEST_EXCEPTION
        e is ServerResponseException -> ERROR_HTTP_SERVER_RESPONSE_EXCEPTION
        e is JsonConvertException -> ERROR_HTTP_JSON_CONVERT_EXCEPTION
        e is HttpRequestTimeoutException -> ERROR_HTTP_REQUEST_TIMEOUT
        e.message.orEmpty() == NO_INTERNET -> NO_INTERNET
        else -> "Something went wrong"
    }

    val statusCode = (e as? ResponseException)?.response?.status?.value ?: 0

    return Resource.Error(
        DomainException.ApiException(
            code = statusCode,
            message = errorMessage,
        ),
    )
}

// ─── Internal Helpers ─────────────────────────────────────────────────────────

/**
 * Extracts the `"result"` field value from a JSON response string.
 * Returns null if the field is not present or the string is not valid JSON.
 */
fun getResultValue(httpResponse: String?): String? {
    if (httpResponse.isNullOrBlank()) return null
    return try {
        lenientJson.parseToJsonElement(httpResponse)
            .jsonObject["result"]
            ?.jsonPrimitive?.content
    } catch (_: Exception) {
        null
    }
}

/**
 * Returns a [Throwable] that best represents the given HTTP [statusCode].
 */
fun getHttpRequestException(statusCode: HttpStatusCode): Throwable {
    return DomainException.ApiException(
        code = statusCode.value,
        message = statusCode.description,
    )
}

/**
 * Returns a string error type label for the given HTTP [statusCode].
 */
fun getHttpErrorType(statusCode: HttpStatusCode): String {
    return when (statusCode.value) {
        in 300..399 -> "RedirectionError"
        in 400..499 -> "ClientError"
        in 500..599 -> "ServerError"
        else -> "UnknownHttpError"
    }
}

/**
 * Marker exception for JSON deserialization failures.
 * Ktor throws `JsonConvertException` which we alias here for clarity.
 */
typealias JsonConvertException = kotlinx.serialization.SerializationException
