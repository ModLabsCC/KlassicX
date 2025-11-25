package cc.modlabs.klassicx.wrappers.transForge

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * Base class for Transforge API clients.
 * Provides shared functionality for making HTTP requests and handling responses.
 */
abstract class TransForgeBaseAPI(
    protected val baseUrl: String,
    protected val httpClient: HttpClient,
) {
    protected val gson = Gson()

    /**
     * Ensures the base URL has no trailing slash.
     */
    protected fun normalizeBaseUrl(url: String): String =
        if (url.endsWith("/")) url.dropLast(1) else url

    /**
     * Builds a full URL from a path.
     */
    protected fun buildUrl(path: String): String {
        val normalized = normalizeBaseUrl(baseUrl)
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        return "$normalized$cleanPath"
    }

    /**
     * Executes a request that expects no response body (void).
     */
    protected suspend fun executeRequestVoid(
        request: HttpRequest
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when {
                response.statusCode() in 200..299 -> Result.success(Unit)
                else -> Result.failure(
                    TransForgeException(
                        "Request failed with status ${response.statusCode()}",
                        response.statusCode(),
                        response.body()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes a request that expects a single object response.
     */
    protected suspend fun <T> executeRequest(
        request: HttpRequest,
        responseType: Class<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when {
                response.statusCode() in 200..299 -> {
                    val body = response.body()
                    if (body.isBlank()) {
                        Result.failure(TransForgeException("Empty response body", response.statusCode()))
                    } else {
                        val parsed = gson.fromJson(body, responseType)
                        Result.success(parsed)
                    }
                }
                else -> Result.failure(
                    TransForgeException(
                        "Request failed with status ${response.statusCode()}",
                        response.statusCode(),
                        response.body()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes a request that expects a list response.
     */
    protected suspend fun <T> executeRequestList(
        request: HttpRequest,
        responseType: TypeToken<List<T>>
    ): Result<List<T>> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when {
                response.statusCode() in 200..299 -> {
                    val body = response.body()
                    val parsed: List<T> = gson.fromJson(body, responseType.type)
                    Result.success(parsed)
                }
                else -> Result.failure(
                    TransForgeException(
                        "Request failed with status ${response.statusCode()}",
                        response.statusCode(),
                        response.body()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Executes a request that expects a map response (for export endpoints).
     */
    protected suspend fun executeRequestMap(
        request: HttpRequest
    ): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            when {
                response.statusCode() in 200..299 -> {
                    val type = object : TypeToken<Map<String, String>>() {}
                    val parsed: Map<String, String> = gson.fromJson(response.body(), type.type)
                    Result.success(parsed)
                }
                else -> Result.failure(
                    TransForgeException(
                        "Request failed with status ${response.statusCode()}",
                        response.statusCode(),
                        response.body()
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates a JSON body from an object.
     */
    protected fun <T> createJsonBody(data: T): String = gson.toJson(data)
}

