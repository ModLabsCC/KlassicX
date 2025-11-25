package cc.modlabs.klassicx.wrappers.transForge

import com.google.gson.reflect.TypeToken
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest

/**
 * API client for team related endpoints.
 */
class TransForgeTeamsAPI(
    baseUrl: String,
    httpClient: HttpClient
) : TransForgeBaseAPI(baseUrl, httpClient) {

    // ==================== Teams Endpoints ====================

    /**
     * Get all teams
     * GET /teams
     */
    suspend fun getTeams(): Result<List<TeamResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams")))
            .GET()
            .build()
        val type = object : TypeToken<List<TeamResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Create a new team
     * POST /teams
     */
    suspend fun createTeam(request: CreateTeamRequest): Result<TeamResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, TeamResponse::class.java)
    }

    /**
     * Delete a team
     * DELETE /teams/{id}
     */
    suspend fun deleteTeam(id: String): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams/$id")))
            .DELETE()
            .build()
        return executeRequestVoid(request)
    }

    /**
     * Get team members
     * GET /teams/{teamId}/members
     */
    suspend fun getTeamMembers(teamId: String): Result<List<TeamMemberResponse>> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams/$teamId/members")))
            .GET()
            .build()
        val type = object : TypeToken<List<TeamMemberResponse>>() {}
        return executeRequestList(request, type)
    }

    /**
     * Add team member
     * POST /teams/{teamId}/members
     */
    suspend fun addTeamMember(teamId: String, request: AddTeamMemberRequest): Result<TeamMemberResponse> {
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams/$teamId/members")))
            .POST(HttpRequest.BodyPublishers.ofString(createJsonBody(request)))
            .header("Content-Type", "application/json")
            .build()
        return executeRequest(httpRequest, TeamMemberResponse::class.java)
    }

    /**
     * Remove team member
     * DELETE /teams/{teamId}/members/{userId}
     */
    suspend fun removeTeamMember(teamId: String, userId: String): Result<Unit> {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(buildUrl("/teams/$teamId/members/$userId")))
            .DELETE()
            .build()
        return executeRequestVoid(request)
    }
}

