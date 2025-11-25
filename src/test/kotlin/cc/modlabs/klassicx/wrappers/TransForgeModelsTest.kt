package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.*
import com.google.gson.Gson
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TransForgeModelsTest {

    private val gson = Gson()

    @Test
    fun testHealthResponseSerialization() {
        val response = HealthResponse("healthy")
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, HealthResponse::class.java)
        
        assertEquals("healthy", deserialized.status)
    }

    @Test
    fun testLoginRequestSerialization() {
        val request = LoginRequest("test@example.com", "password123", "123456")
        val json = gson.toJson(request)
        val deserialized = gson.fromJson(json, LoginRequest::class.java)
        
        assertEquals("test@example.com", deserialized.email)
        assertEquals("password123", deserialized.password)
        assertEquals("123456", deserialized.totpCode)
    }

    @Test
    fun testLoginResponseSerialization() {
        val user = UserResponse("1", "test@example.com", "Test User", false)
        val response = LoginResponse(user, false, null)
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, LoginResponse::class.java)
        
        assertNotNull(deserialized.user)
        assertEquals("test@example.com", deserialized.user?.email)
        assertFalse(deserialized.requires2FA)
    }

    @Test
    fun testTranslationResponseSerialization() {
        val response = TranslationResponse(
            "trans-1",
            "My Translation",
            "user-1",
            null,
            "2024-01-01T00:00:00Z"
        )
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, TranslationResponse::class.java)
        
        assertEquals("trans-1", deserialized.id)
        assertEquals("My Translation", deserialized.name)
        assertEquals("user-1", deserialized.ownerUserId)
        assertNull(deserialized.ownerTeamId)
    }

    @Test
    fun testCreateTranslationRequestSerialization() {
        val request = CreateTranslationRequest("New Translation", "team-1")
        val json = gson.toJson(request)
        val deserialized = gson.fromJson(json, CreateTranslationRequest::class.java)
        
        assertEquals("New Translation", deserialized.name)
        assertEquals("team-1", deserialized.ownerTeamId)
    }

    @Test
    fun testTeamResponseSerialization() {
        val response = TeamResponse(
            "team-1",
            "My Team",
            "user-1",
            "2024-01-01T00:00:00Z"
        )
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, TeamResponse::class.java)
        
        assertEquals("team-1", deserialized.id)
        assertEquals("My Team", deserialized.name)
        assertEquals("user-1", deserialized.ownerUserId)
    }

    @Test
    fun testSuggestionResponseSerialization() {
        val response = SuggestionResponse(
            "sug-1",
            "trans-1",
            "key-1",
            "test.key",
            "en",
            "Suggested value",
            "pending",
            "user-1",
            "2024-01-01T00:00:00Z",
            null,
            null
        )
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, SuggestionResponse::class.java)
        
        assertEquals("sug-1", deserialized.id)
        assertEquals("trans-1", deserialized.translationId)
        assertEquals("pending", deserialized.status)
    }

    @Test
    fun testGridDataResponseSerialization() {
        val keys = listOf(KeyResponse("key-1", "test.key", "2024-01-01T00:00:00Z"))
        val locales = listOf(LocaleResponse("loc-1", "trans-1", "en", true, "2024-01-01T00:00:00Z"))
        val values = listOf(ValueResponse("val-1", "trans-1", "key-1", "en", "Value", "2024-01-01T00:00:00Z", "2024-01-01T00:00:00Z"))
        
        val response = GridDataResponse(keys, locales, values)
        val json = gson.toJson(response)
        val deserialized = gson.fromJson(json, GridDataResponse::class.java)
        
        assertEquals(1, deserialized.keys.size)
        assertEquals(1, deserialized.enabledLocales.size)
        assertEquals(1, deserialized.values.size)
    }
}

