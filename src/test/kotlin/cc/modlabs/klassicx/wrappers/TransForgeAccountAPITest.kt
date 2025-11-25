package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.http.HttpClient

class TransForgeAccountAPITest {

    @Test
    fun testAccountAPICreation() {
        val api = TransForgeAccountAPI("https://api.example.com", HttpClient.newHttpClient())
        assertNotNull(api)
    }

    @Test
    fun testUpdatePasswordRequest() {
        val request = UpdatePasswordRequest("oldPass", "newPass")
        assertEquals("oldPass", request.currentPassword)
        assertEquals("newPass", request.newPassword)
    }

    @Test
    fun testUpdateUsernameRequest() {
        val request = UpdateUsernameRequest("newusername")
        assertEquals("newusername", request.username)
    }

    @Test
    fun testDisableTotpRequest() {
        val request = DisableTotpRequest("123456")
        assertEquals("123456", request.code)
    }

    @Test
    fun testEnableTotpRequest() {
        val request = EnableTotpRequest("123456")
        assertEquals("123456", request.code)
    }

    @Test
    fun testRegenerateBackupCodesRequest() {
        val request = RegenerateBackupCodesRequest("123456")
        assertEquals("123456", request.totpCode)
    }
}

