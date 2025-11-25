package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class TransForgeTeamsAPITest {

    @Test
    fun testTeamsAPICreation() {
        val api = TransForgeTeamsAPI("https://api.example.com", HttpClient.newHttpClient())
        assertNotNull(api)
    }

    @Test
    fun testCreateTeamRequest() {
        val request = CreateTeamRequest("My Team")
        assertEquals("My Team", request.name)
    }

    @Test
    fun testAddTeamMemberRequest() {
        val request = AddTeamMemberRequest("test@example.com", "admin")
        assertEquals("test@example.com", request.email)
        assertEquals("admin", request.role)
    }

    @Test
    fun testAddTeamMemberRequestWithNullRole() {
        val request = AddTeamMemberRequest("test@example.com", null)
        assertEquals("test@example.com", request.email)
        assertNull(request.role)
    }

    @Test
    fun testTeamResponse() {
        val response = TeamResponse(
            "team-1",
            "My Team",
            "user-1",
            "2024-01-01T00:00:00Z"
        )
        assertEquals("team-1", response.id)
        assertEquals("My Team", response.name)
        assertEquals("user-1", response.ownerUserId)
        assertEquals("2024-01-01T00:00:00Z", response.createdAt)
    }

    @Test
    fun testTeamMemberResponse() {
        val response = TeamMemberResponse(
            "member-1",
            "team-1",
            "user-1",
            "Test User",
            "test@example.com",
            "admin",
            "2024-01-01T00:00:00Z"
        )
        assertEquals("member-1", response.id)
        assertEquals("team-1", response.teamId)
        assertEquals("user-1", response.userId)
        assertEquals("Test User", response.userName)
        assertEquals("test@example.com", response.userEmail)
        assertEquals("admin", response.role)
        assertEquals("2024-01-01T00:00:00Z", response.joinedAt)
    }
}

