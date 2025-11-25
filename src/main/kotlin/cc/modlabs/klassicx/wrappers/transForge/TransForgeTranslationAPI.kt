package cc.modlabs.klassicx.wrappers.transForge

import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

/**
 * API client for translation and suggestion related endpoints.
 */
class TransForgeTranslationAPI(
    baseUrl: String,
    httpClient: HttpClient
) : TransForgeBaseAPI(baseUrl, httpClient) {

    // ==================== API Endpoints ====================

    /**
     * Render MiniMessage to HTML
     * POST /api/minimessage/render
     */
    suspend fun renderMiniMessage(request: MiniMessageRenderRequest): Result<MiniMessageRenderResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/minimessage/render")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, MiniMessageRenderResponse::class.java)
    }

    // ==================== Suggestions Endpoints ====================

    /**
     * Approve a suggestion
     * POST /api/suggestions/{id}/approve
     */
    suspend fun approveSuggestion(
        id: String,
        request: ApproveSuggestionRequest? = null
    ): Result<SuggestionResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/suggestions/$id/approve")))
            .POST(
                if (request != null) {
                    HttpRequest.BodyPublishers.ofString(createJsonBody(request))
                } else {
                    HttpRequest.BodyPublishers.noBody()
                }
            )
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, SuggestionResponse::class.java)
    }

    /**
     * Reject a suggestion
     * POST /api/suggestions/{id}/reject
     */
    suspend fun rejectSuggestion(
        id: String,
        request: RejectSuggestionRequest? = null
    ): Result<SuggestionResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/suggestions/$id/reject")))
            .POST(
                if (request != null) {
                    HttpRequest.BodyPublishers.ofString(createJsonBody(request))
                } else {
                    HttpRequest.BodyPublishers.noBody()
                }
            )
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, SuggestionResponse::class.java)
    }

    // ==================== Translations Endpoints ====================

    /**
     * Get all translations
     * GET /api/translations
     */
    suspend fun getTranslations(
        ownerUserId: String? = null,
        ownerTeamId: String? = null
    ): Result<List<TranslationResponse>> {
        val uri = buildUrl("/api/translations").let { base ->
            val params = mutableListOf<String>()
            ownerUserId?.let { params.add("ownerUserId=$it") }
            ownerTeamId?.let { params.add("ownerTeamId=$it") }
            if (params.isNotEmpty()) "$base?${params.joinToString("&")}" else base
        }
        val request = HttpRequest.newBuilder()
            .uri(URI.create(uri))
            .GET()
            .build()
        val type = object : TypeToken<List<TranslationResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Create a new translation
     * POST /api/translations
     */
    suspend fun createTranslation(request: CreateTranslationRequest): Result<TranslationResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, TranslationResponse::class.java)
    }

    /**
     * Delete a translation
     * DELETE /api/translations/{id}
     */
    suspend fun deleteTranslation(id: String): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$id")))
            .DELETE()
            .build()
        return executeRequestVoid(request)
    }

    /**
     * Transfer translation ownership
     * PATCH /api/translations/{id}/transfer
     */
    suspend fun transferTranslationOwnership(
        id: String,
        request: TransferOwnershipRequest
    ): Result<TranslationResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$id/transfer")))
            .method("PATCH", HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, TranslationResponse::class.java)
    }

    /**
     * Export translation as JSON
     * GET /api/translations/{translationId}/export/{locale}
     */
    suspend fun exportTranslation(translationId: String, locale: String): Result<Map<String, String>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/export/$locale")))
            .GET()
            .build()
        return executeRequestMap(request)
    }

    /**
     * Get grid data for a translation
     * GET /api/translations/{translationId}/grid
     */
    suspend fun getTranslationGrid(translationId: String): Result<GridDataResponse> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/grid")))
            .GET()
            .build()
        return executeRequest(request, GridDataResponse::class.java)
    }

    /**
     * Import translations from JSON
     * POST /api/translations/{translationId}/import/{locale}
     */
    suspend fun importTranslation(
        translationId: String,
        locale: String,
        translations: Map<String, Any>
    ): Result<ImportResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/import/$locale")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(translations)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, ImportResponse::class.java)
    }

    /**
     * Get all translation keys for a translation
     * GET /api/translations/{translationId}/keys
     */
    suspend fun getTranslationKeys(translationId: String): Result<List<KeyResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/keys")))
            .GET()
            .build()
        val type = object : TypeToken<List<KeyResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Create a new translation key
     * POST /api/translations/{translationId}/keys
     */
    suspend fun createTranslationKey(
        translationId: String,
        request: CreateKeyRequest
    ): Result<KeyResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/keys")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, KeyResponse::class.java)
    }

    /**
     * Delete a translation key
     * DELETE /api/translations/{translationId}/keys/{id}
     */
    suspend fun deleteTranslationKey(translationId: String, id: String): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/keys/$id")))
            .DELETE()
            .build()
        return executeRequestVoid(request)
    }

    /**
     * Get enabled locales for a translation
     * GET /api/translations/{translationId}/locales
     */
    suspend fun getTranslationLocales(translationId: String): Result<List<LocaleResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/locales")))
            .GET()
            .build()
        val type = object : TypeToken<List<LocaleResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Set locale enabled/disabled
     * POST /api/translations/{translationId}/locales
     */
    suspend fun setTranslationLocale(
        translationId: String,
        request: SetLocaleRequest
    ): Result<LocaleResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/locales")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, LocaleResponse::class.java)
    }

    /**
     * Get permissions for a translation
     * GET /api/translations/{translationId}/permissions
     */
    suspend fun getTranslationPermissions(translationId: String): Result<List<PermissionResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/permissions")))
            .GET()
            .build()
        val type = object : TypeToken<List<PermissionResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Grant permission
     * POST /api/translations/{translationId}/permissions
     */
    suspend fun grantTranslationPermission(
        translationId: String,
        request: GrantPermissionRequest
    ): Result<PermissionResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/permissions")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, PermissionResponse::class.java)
    }

    /**
     * Update permission
     * PATCH /api/translations/{translationId}/permissions/{id}
     */
    suspend fun updateTranslationPermission(
        translationId: String,
        id: String,
        request: UpdatePermissionRequest
    ): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/permissions/$id")))
            .method("PATCH", HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }

    /**
     * Revoke permission
     * DELETE /api/translations/{translationId}/permissions/{id}
     */
    suspend fun revokeTranslationPermission(translationId: String, id: String): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/permissions/$id")))
            .DELETE()
            .build()
        return executeRequestVoid(request)
    }

    /**
     * Submit a translation suggestion
     * POST /api/translations/{translationId}/suggestions
     */
    suspend fun submitSuggestion(
        translationId: String,
        request: SubmitSuggestionRequest
    ): Result<SuggestionResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/suggestions")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, SuggestionResponse::class.java)
    }

    /**
     * List suggestions for a translation
     * GET /api/translations/{translationId}/suggestions
     */
    suspend fun getSuggestions(translationId: String): Result<List<SuggestionResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/suggestions")))
            .GET()
            .build()
        val type = object : TypeToken<List<SuggestionResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Set a translation value
     * POST /api/translations/{translationId}/values
     */
    suspend fun setTranslationValue(
        translationId: String,
        request: SetValueRequest
    ): Result<ValueResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/api/translations/$translationId/values")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, ValueResponse::class.java)
    }
}

