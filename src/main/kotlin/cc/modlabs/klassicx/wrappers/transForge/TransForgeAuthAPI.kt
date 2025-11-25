package cc.modlabs.klassicx.wrappers.transForge

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

/**
 * API client for authentication and health check endpoints.
 */
class TransForgeAuthAPI(
    baseUrl: String,
    httpClient: HttpClient
) : TransForgeBaseAPI(baseUrl, httpClient) {

    // ==================== Auth Endpoints ====================

    /**
     * Login
     * POST /auth/login
     */
    suspend fun login(request: LoginRequest): Result<LoginResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/auth/login")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, LoginResponse::class.java)
    }

    /**
     * Logout
     * POST /auth/logout
     */
    suspend fun logout(): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/auth/logout")))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()
        return executeRequestVoid(request)
    }

    /**
     * Get current user
     * GET /auth/me
     */
    suspend fun getCurrentUser(): Result<UserResponse> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/auth/me")))
            .GET()
            .build()
        return executeRequest(request, UserResponse::class.java)
    }

    /**
     * Register a new user
     * POST /auth/register
     */
    suspend fun register(request: RegisterRequest): Result<UserResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/auth/register")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, UserResponse::class.java)
    }

    // ==================== Health Endpoint ====================

    /**
     * Health check
     * GET /health
     */
    suspend fun healthCheck(): Result<HealthResponse> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/health")))
            .GET()
            .build()
        return executeRequest(request, HealthResponse::class.java)
    }
}

