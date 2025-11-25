package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class TransForgeTranslationAPITest {

    @Test
    fun testTranslationAPICreation() {
        val api = TransForgeTranslationAPI("https://api.example.com", HttpClient.newHttpClient())
        assertNotNull(api)
    }

    @Test
    fun testCreateTranslationRequest() {
        val request = CreateTranslationRequest("My Translation")
        assertEquals("My Translation", request.name)
        assertNull(request.ownerTeamId)
    }

    @Test
    fun testCreateTranslationRequestWithTeam() {
        val request = CreateTranslationRequest("My Translation", "team-1")
        assertEquals("My Translation", request.name)
        assertEquals("team-1", request.ownerTeamId)
    }

    @Test
    fun testCreateKeyRequest() {
        val request = CreateKeyRequest("test.key")
        assertEquals("test.key", request.key)
    }

    @Test
    fun testSetLocaleRequest() {
        val request = SetLocaleRequest("en", true)
        assertEquals("en", request.locale)
        assertTrue(request.enabled)
    }

    @Test
    fun testSetValueRequest() {
        val request = SetValueRequest("key-1", "en", "Value")
        assertEquals("key-1", request.keyId)
        assertEquals("en", request.locale)
        assertEquals("Value", request.value)
    }

    @Test
    fun testSetValueRequestWithNull() {
        val request = SetValueRequest("key-1", "en", null)
        assertEquals("key-1", request.keyId)
        assertEquals("en", request.locale)
        assertNull(request.value)
    }

    @Test
    fun testSubmitSuggestionRequest() {
        val request = SubmitSuggestionRequest("test.key", "en", "Suggested value")
        assertEquals("test.key", request.key)
        assertEquals("en", request.locale)
        assertEquals("Suggested value", request.value)
    }

    @Test
    fun testApproveSuggestionRequest() {
        val request = ApproveSuggestionRequest("Approved value")
        assertEquals("Approved value", request.value)
    }

    @Test
    fun testApproveSuggestionRequestWithNull() {
        val request = ApproveSuggestionRequest(null)
        assertNull(request.value)
    }

    @Test
    fun testRejectSuggestionRequest() {
        val request = RejectSuggestionRequest("Rejected")
        assertEquals("Rejected", request.notes)
    }

    @Test
    fun testTransferOwnershipRequest() {
        val request = TransferOwnershipRequest("user-1", null)
        assertEquals("user-1", request.newOwnerUserId)
        assertNull(request.newOwnerTeamId)
    }

    @Test
    fun testGrantPermissionRequest() {
        val request = GrantPermissionRequest("user-1", null, "admin")
        assertEquals("user-1", request.userId)
        assertNull(request.teamId)
        assertEquals("admin", request.role)
    }

    @Test
    fun testUpdatePermissionRequest() {
        val request = UpdatePermissionRequest("editor")
        assertEquals("editor", request.role)
    }

    @Test
    fun testMiniMessageRenderRequest() {
        val request = MiniMessageRenderRequest("<red>Hello</red>", true)
        assertEquals("<red>Hello</red>", request.text)
        assertTrue(request.isolateNewlines)
    }

    @Test
    fun testMiniMessageRenderResponse() {
        val response = MiniMessageRenderResponse("<p>Hello</p>", true, null)
        assertEquals("<p>Hello</p>", response.html)
        assertTrue(response.success)
        assertNull(response.errorMessage)
    }
}

