package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class TransForgeAuthAPITest {

    @Test
    fun testAuthAPICreation() {
        val api = TransForgeAuthAPI("https://api.example.com", HttpClient.newHttpClient())
        assertNotNull(api)
    }

    @Test
    fun testLoginRequest() {
        val request = LoginRequest("test@example.com", "password123")
        assertEquals("test@example.com", request.email)
        assertEquals("password123", request.password)
        assertNull(request.totpCode)
    }

    @Test
    fun testLoginRequestWithTotp() {
        val request = LoginRequest("test@example.com", "password123", "123456")
        assertEquals("test@example.com", request.email)
        assertEquals("password123", request.password)
        assertEquals("123456", request.totpCode)
    }

    @Test
    fun testRegisterRequest() {
        val request = RegisterRequest("test@example.com", "password123", "Test User")
        assertEquals("test@example.com", request.email)
        assertEquals("password123", request.password)
        assertEquals("Test User", request.name)
    }

    @Test
    fun testUserResponse() {
        val user = UserResponse("1", "test@example.com", "Test User", false)
        assertEquals("1", user.id)
        assertEquals("test@example.com", user.email)
        assertEquals("Test User", user.name)
        assertFalse(user.totpEnabled)
    }

    @Test
    fun testLoginResponse() {
        val user = UserResponse("1", "test@example.com", "Test User", false)
        val response = LoginResponse(user, false, null)
        
        assertNotNull(response.user)
        assertFalse(response.requires2FA)
        assertNull(response.message)
    }

    @Test
    fun testLoginResponseWith2FA() {
        val response = LoginResponse(null, true, "2FA required")
        
        assertNull(response.user)
        assertTrue(response.requires2FA)
        assertEquals("2FA required", response.message)
    }

    @Test
    fun testHealthResponse() {
        val response = HealthResponse("healthy")
        assertEquals("healthy", response.status)
    }
}

