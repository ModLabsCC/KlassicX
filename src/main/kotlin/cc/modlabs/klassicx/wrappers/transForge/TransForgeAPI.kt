package cc.modlabs.klassicx.wrappers.transForge

import java.net.CookieManager
import java.net.http.HttpClient
import java.time.Duration

/**
 * Complete wrapper for the Transforge API.
 * Provides type-safe access to all endpoints defined in the OpenAPI specification.
 *
 * This class composes multiple specialized API clients:
 * - [account] - Account and 2FA endpoints
 * - [auth] - Authentication and health check endpoints
 * - [translations] - Translation and suggestion endpoints
 * - [teams] - Team management endpoints
 *
 * @param baseUrl Base URL of the Transforge API, e.g. "https://transforge.example.com"
 * @param httpClient Optional custom HttpClient, defaults to HttpClient.newHttpClient()
 */
class TransForgeAPI(
    baseUrl: String,
    httpClient: HttpClient = HttpClient.newBuilder()
        .cookieHandler(CookieManager())
        .connectTimeout(Duration.ofSeconds(30))
        .build(),
) {
    /**
     * Account and 2FA related endpoints.
     */
    val account = TransForgeAccountAPI(baseUrl, httpClient)

    /**
     * Authentication and health check endpoints.
     */
    val auth = TransForgeAuthAPI(baseUrl, httpClient)

    /**
     * Translation and suggestion related endpoints.
     */
    val translations = TransForgeTranslationAPI(baseUrl, httpClient)

    /**
     * Team management endpoints.
     */
    val teams = TransForgeTeamsAPI(baseUrl, httpClient)

    // ==================== Convenience Methods (Backward Compatibility) ====================
    // These methods delegate to the sub-APIs for backward compatibility

    // Account 2FA
    suspend fun getBackupCodesCount() = account.getBackupCodesCount()
    suspend fun regenerateBackupCodes(request: RegenerateBackupCodesRequest) = account.regenerateBackupCodes(request)
    suspend fun disableTotp(request: DisableTotpRequest) = account.disableTotp(request)
    suspend fun enableTotp(request: EnableTotpRequest) = account.enableTotp(request)
    suspend fun setupTotp() = account.setupTotp()
    suspend fun updatePassword(request: UpdatePasswordRequest) = account.updatePassword(request)
    suspend fun updateUsername(request: UpdateUsernameRequest) = account.updateUsername(request)

    // Auth
    suspend fun login(request: LoginRequest) = auth.login(request)
    suspend fun logout() = auth.logout()
    suspend fun getCurrentUser() = auth.getCurrentUser()
    suspend fun register(request: RegisterRequest) = auth.register(request)
    suspend fun healthCheck() = auth.healthCheck()

    // Translations
    suspend fun renderMiniMessage(request: MiniMessageRenderRequest) = translations.renderMiniMessage(request)
    suspend fun approveSuggestion(id: String, request: ApproveSuggestionRequest? = null) = translations.approveSuggestion(id, request)
    suspend fun rejectSuggestion(id: String, request: RejectSuggestionRequest? = null) = translations.rejectSuggestion(id, request)
    suspend fun getTranslations(ownerUserId: String? = null, ownerTeamId: String? = null) = translations.getTranslations(ownerUserId, ownerTeamId)
    suspend fun createTranslation(request: CreateTranslationRequest) = translations.createTranslation(request)
    suspend fun deleteTranslation(id: String) = translations.deleteTranslation(id)
    suspend fun transferTranslationOwnership(id: String, request: TransferOwnershipRequest) = translations.transferTranslationOwnership(id, request)
    suspend fun exportTranslation(translationId: String, locale: String) = translations.exportTranslation(translationId, locale)
    suspend fun getTranslationGrid(translationId: String) = translations.getTranslationGrid(translationId)
    suspend fun importTranslation(translationId: String, locale: String, translationMap: Map<String, Any>) = translations.importTranslation(translationId, locale, translationMap)
    suspend fun getTranslationKeys(translationId: String) = translations.getTranslationKeys(translationId)
    suspend fun createTranslationKey(translationId: String, request: CreateKeyRequest) = translations.createTranslationKey(translationId, request)
    suspend fun deleteTranslationKey(translationId: String, id: String) = translations.deleteTranslationKey(translationId, id)
    suspend fun getTranslationLocales(translationId: String) = translations.getTranslationLocales(translationId)
    suspend fun setTranslationLocale(translationId: String, request: SetLocaleRequest) = translations.setTranslationLocale(translationId, request)
    suspend fun getTranslationPermissions(translationId: String) = translations.getTranslationPermissions(translationId)
    suspend fun grantTranslationPermission(translationId: String, request: GrantPermissionRequest) = translations.grantTranslationPermission(translationId, request)
    suspend fun updateTranslationPermission(translationId: String, id: String, request: UpdatePermissionRequest) = translations.updateTranslationPermission(translationId, id, request)
    suspend fun revokeTranslationPermission(translationId: String, id: String) = translations.revokeTranslationPermission(translationId, id)
    suspend fun submitSuggestion(translationId: String, request: SubmitSuggestionRequest) = translations.submitSuggestion(translationId, request)
    suspend fun getSuggestions(translationId: String) = translations.getSuggestions(translationId)
    suspend fun setTranslationValue(translationId: String, request: SetValueRequest) = translations.setTranslationValue(translationId, request)

    // Teams
    suspend fun getTeams() = teams.getTeams()
    suspend fun createTeam(request: CreateTeamRequest) = teams.createTeam(request)
    suspend fun deleteTeam(id: String) = teams.deleteTeam(id)
    suspend fun getTeamMembers(teamId: String) = teams.getTeamMembers(teamId)
    suspend fun addTeamMember(teamId: String, request: AddTeamMemberRequest) = teams.addTeamMember(teamId, request)
    suspend fun removeTeamMember(teamId: String, userId: String) = teams.removeTeamMember(teamId, userId)
}

