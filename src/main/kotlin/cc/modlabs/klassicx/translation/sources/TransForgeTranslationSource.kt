package cc.modlabs.klassicx.translation.sources

import cc.modlabs.klassicx.extensions.getInternalKlassicxLogger
import cc.modlabs.klassicx.tools.Environment.isDevLoggingEnabled
import cc.modlabs.klassicx.translation.Translation
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import cc.modlabs.klassicx.translation.live.HelloEvent
import cc.modlabs.klassicx.translation.live.KeyCreatedEvent
import cc.modlabs.klassicx.translation.live.KeyDeletedEvent
import cc.modlabs.klassicx.translation.live.KeyUpdatedEvent
import cc.modlabs.klassicx.translation.live.LiveUpdateEvent
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.util.concurrent.CompletionStage

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
 * @param apiKey Optional API key for authenticated access. If provided, it will be sent via
 *               Authorization header and as X-API-Key; for WebSocket also as query param.
 * @param httpClient Optional custom HttpClient, defaults to HttpClient.newHttpClient().
 */
class TransForgeTranslationSource(
    private val baseUrl: String,
    private val translationId: String,
    private val apiKey: String? = null,
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
        val builder = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    normalizeBaseUrl(baseUrl) +
                            "/api/translations/$translationId/locales"
                )
            )
            .GET()

        if (!apiKey.isNullOrBlank()) {
            builder.header("X-API-Key", apiKey)
        }
        val request = builder.build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            // In case of error, return no languages instead of failing the whole manager
            return@withContext emptyList()
        }

        val type = object : TypeToken<List<LocaleResponse>>() {}.type
        val locales: List<LocaleResponse> = gson.fromJson(response.body(), type)

        if (isDevLoggingEnabled) {
            getInternalKlassicxLogger().info("Discovered languages for translationId $translationId: ${locales.joinToString { "${it.locale}(enabled: ${it.enabled} - id: ${it.id} - ${it.createdAt})" }}")
        }

        locales
            .filter { it.enabled }
            .map { it.locale }
            .distinct()
    }

    override suspend fun getTranslations(language: String): List<Translation> = withContext(Dispatchers.IO) {
        val builder = HttpRequest.newBuilder()
            .uri(
                URI.create(
                    normalizeBaseUrl(baseUrl) +
                            "/api/translations/$translationId/export/$language"
                )
            )
            .GET()

        if (!apiKey.isNullOrBlank()) {
            builder.header("X-API-Key", apiKey)
        }
        val request = builder.build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() !in 200..299) {
            return@withContext emptyList()
        }

        // Expected response: a flat JSON object: { "some.key": "Value", "other.key": "Other value", ... }
        val type = object : TypeToken<Map<String, String>>() {}.type
        val data: Map<String, String> = gson.fromJson(response.body(), type) ?: emptyMap()

        data.map { (key, value) ->
            if(isDevLoggingEnabled) {
                getInternalKlassicxLogger().info("Translating $key to $value in $language")
            }
            Translation (
                languageCode = language,
                messageKey = key,
                message = value
            )
        }
    }

    override fun liveUpdates(): Flow<LiveUpdateEvent>? {
        // Provide live updates only if baseUrl and translationId are set (always) and apiKey is optional
        val wsUri = buildWsUri()

        return callbackFlow {
            val listener = object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    webSocket.request(1)
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                    try {
                        val json = data.toString()
                        val obj = gson.fromJson(json, Map::class.java) as Map<*, *>
                        val type = obj["type"] as? String
                        // App-level keepalive: server may send {"type":"ping"}; reply with {"type":"pong"}
                        if (type == "ping") {
                            try {
                                webSocket.sendText("{" + '"'.toString() + "type" + '"'.toString() + ":" + '"'.toString() + "pong" + '"'.toString() + "}", true)
                            } catch (_: Throwable) {
                                // ignore failures to respond to ping
                            }
                        }
                        val event: LiveUpdateEvent? = when (type) {
                            "hello" -> HelloEvent(
                                translationId = (obj["translationId"] as? String) ?: translationId,
                                permission = (obj["permission"] as? String) ?: "READ"
                            )
                            "key_created" -> KeyCreatedEvent(
                                translationId = (obj["translationId"] as? String) ?: translationId,
                                keyId = (obj["keyId"] as? String) ?: "",
                                key = (obj["key"] as? String) ?: "",
                                ts = (obj["ts"] as? String) ?: ""
                            )
                            "key_deleted" -> KeyDeletedEvent(
                                translationId = (obj["translationId"] as? String) ?: translationId,
                                keyId = (obj["keyId"] as? String) ?: "",
                                ts = (obj["ts"] as? String) ?: ""
                            )
                            "key_updated" -> KeyUpdatedEvent(
                                translationId = (obj["translationId"] as? String) ?: translationId,
                                keyId = (obj["keyId"] as? String) ?: "",
                                locale = (obj["locale"] as? String) ?: "",
                                value = obj["value"] as? String,
                                ts = (obj["ts"] as? String) ?: ""
                            )
                            // "ping" handled above for keepalive; no event to emit
                            else -> null
                        }
                        if (event != null) trySend(event).isSuccess
                    } catch (_: Throwable) {
                        // ignore malformed frames
                    } finally {
                        webSocket.request(1)
                    }
                    return null
                }

                override fun onBinary(webSocket: WebSocket, data: ByteBuffer, last: Boolean): CompletionStage<*>? {
                    webSocket.request(1)
                    return null
                }

                // Handle WebSocket control-frame ping/pong at protocol level (separate from JSON ping/pong messages)
                override fun onPing(webSocket: WebSocket, message: ByteBuffer): CompletionStage<*>? {
                    try {
                        webSocket.sendPong(message)
                    } catch (_: Throwable) {
                        // ignore failures
                    } finally {
                        webSocket.request(1)
                    }
                    return null
                }

                override fun onPong(webSocket: WebSocket, message: ByteBuffer): CompletionStage<*>? {
                    webSocket.request(1)
                    return null
                }

                override fun onError(webSocket: WebSocket, error: Throwable) {
                    close(error)
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String?): CompletionStage<*>? {
                    close()
                    return null
                }
            }

            val builder = httpClient.newWebSocketBuilder()
            if (!apiKey.isNullOrBlank()) {
                builder.header("X-API-Key", apiKey)
            }
            // Also add query param for redundancy
            val uriWithQuery = if (!apiKey.isNullOrBlank()) URI.create("$wsUri${if (wsUri.contains("?")) "&" else "?"}api-key=${encode(apiKey)}") else URI.create(wsUri)

            val ws = builder.buildAsync(uriWithQuery, listener)

            awaitClose {
                try {
                    ws.get()?.sendClose(WebSocket.NORMAL_CLOSURE, "client-close")
                } catch (_: Throwable) {
                }
            }
        }.buffer(capacity = 64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    }

    /**
     * Ensures the base URL has no trailing slash.
     */
    private fun normalizeBaseUrl(url: String): String =
        if (url.endsWith("/")) url.dropLast(1) else url

    private fun buildWsUri(): String {
        val normalized = normalizeBaseUrl(baseUrl)
        val uri = URI.create(normalized)
        val scheme = when (uri.scheme?.lowercase()) {
            "https" -> "wss"
            "http" -> "ws"
            "wss", "ws" -> uri.scheme
            else -> "wss"
        }
        val authority = uri.authority ?: normalized.removePrefix("${uri.scheme}://")
        val basePath = uri.path?.takeIf { it.isNotBlank() }?.let { if (it.endsWith("/")) it.dropLast(1) else it } ?: ""
        return "$scheme://$authority$basePath/ws/translations/${encode(translationId)}"
    }

    private fun encode(s: String?): String = java.net.URLEncoder.encode(s ?: "", java.nio.charset.StandardCharsets.UTF_8)
}