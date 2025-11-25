package cc.modlabs.klassicx.wrappers.transForge

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

/**
 * API client for account and 2FA related endpoints.
 */
class TransForgeAccountAPI(
    baseUrl: String,
    httpClient: HttpClient
) : TransForgeBaseAPI(baseUrl, httpClient) {

    // ==================== Account 2FA Endpoints ====================

    /**
     * Get backup codes count
     * GET /account/2fa/backup-codes
     */
    suspend fun getBackupCodesCount(): Result<BackupCodesCountResponse> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/2fa/backup-codes")))
            .GET()
            .build()
        return executeRequest(request, BackupCodesCountResponse::class.java)
    }

    /**
     * Regenerate backup codes
     * POST /account/2fa/backup-codes/regenerate
     */
    suspend fun regenerateBackupCodes(request: RegenerateBackupCodesRequest): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/2fa/backup-codes/regenerate")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }

    /**
     * Disable TOTP
     * POST /account/2fa/totp/disable
     */
    suspend fun disableTotp(request: DisableTotpRequest): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/2fa/totp/disable")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }

    /**
     * Enable TOTP
     * POST /account/2fa/totp/enable
     */
    suspend fun enableTotp(request: EnableTotpRequest): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/2fa/totp/enable")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }

    /**
     * Setup TOTP
     * POST /account/2fa/totp/setup
     */
    suspend fun setupTotp(): Result<TotpSetupResponse> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/2fa/totp/setup")))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()
        return executeRequest(request, TotpSetupResponse::class.java)
    }

    // ==================== Account Endpoints ====================

    /**
     * Update password
     * PATCH /account/password
     */
    suspend fun updatePassword(request: UpdatePasswordRequest): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/password")))
            .method("PATCH", HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }

    /**
     * Update username
     * PATCH /account/username
     */
    suspend fun updateUsername(request: UpdateUsernameRequest): Result<Unit> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/account/username")))
            .method("PATCH", HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequestVoid(httpRequest)
    }
}

