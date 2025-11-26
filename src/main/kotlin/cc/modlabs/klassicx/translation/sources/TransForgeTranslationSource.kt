package cc.modlabs.klassicx.translation.sources

import cc.modlabs.klassicx.translation.Translation
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

/**
 * A TranslationSource implementation that loads translations from a Transforge instance.
 *
 * The source uses the Transforge REST API to
 * - discover enabled locales for a translation module
 * - fetch all keys for a given locale as a flat JSON object
 *
 * @param baseUrl Base URL of the Transforge API, e.g. "https://transforge.example.com"
 *                The class will append the path segments itself, no trailing slash required.
 * @param translationId The Transforge translation module ID to use for lookups.
 * @param httpClient Optional custom HttpClient, defaults to HttpClient.newHttpClient().
 */
class TransForgeTranslationSource(
    private val baseUrl: String,
    private val translationId: String,
    private val httpClient: HttpClient = HttpClient.newHttpClient(),
) : TranslationSource {

    private val gson = Gson()

    /**
     * DTO for /api/translations/{translationId}/locales
     */
    private data class LocaleResponse(
        val id: String,
        val translationId: String,
        val locale: String,
        val enabled: Boolean,
        val createdAt: String,
    )

    override suspend fun getLanguages(): List<String> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    normalizeBaseUrl(baseUrl) +
                            "/api/translations/$translationId/locales"
                )
            )
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            // In case of error, return no languages instead of failing the whole manager
            return@withContext emptyList()
        }

        val type = object : TypeToken<List<LocaleResponse>>() {}.type
        val locales: List<LocaleResponse> = gson.fromJson(response.body(), type)

        locales
            .filter { it.enabled }
            .map { it.locale }
            .distinct()
    }

    override suspend fun getTranslations(language: String): List<Translation> = withContext(Dispatchers.IO) {
        val request = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    normalizeBaseUrl(baseUrl) +
                            "/api/translations/$translationId/export/$language"
                )
            )
            .GET()
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            return@withContext emptyList()
        }

        // Expected response: a flat JSON object: { "some.key": "Value", "other.key": "Other value", ... }
        val type = object : TypeToken<Map<String, String>>() {}.type
        val data: Map<String, String> = gson.fromJson(response.body(), type) ?: emptyMap()

        data.map { (key, value) ->
            Translation(
                languageCode = language,
                messageKey = key,
                message = value
            )
        }
    }

    /**
     * Ensures the base URL has no trailing slash.
     */
    private fun normalizeBaseUrl(url: String): String =
        if (url.endsWith("/")) url.dropLast(1) else url
}