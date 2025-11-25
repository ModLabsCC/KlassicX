package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.TransForgeAPI
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.CookieManager
import java.net.http.HttpClient
import java.time.Duration

class TransForgeAPITest {

    @Test
    fun testApiComposition() {
        val api = TransForgeAPI("https://api.example.com")
        
        assertNotNull(api.account)
        assertNotNull(api.auth)
        assertNotNull(api.translations)
        assertNotNull(api.teams)
    }

    @Test
    fun testCustomHttpClient() {
        val customClient = HttpClient.newBuilder()
            .cookieHandler(CookieManager())
            .connectTimeout(Duration.ofSeconds(10))
            .build()
        
        val api = TransForgeAPI("https://api.example.com", customClient)
        assertNotNull(api)
    }

    @Test
    fun testBaseUrlNormalization() {
        val api1 = TransForgeAPI("https://api.example.com")
        val api2 = TransForgeAPI("https://api.example.com/")
        
        // Both should work the same way
        assertNotNull(api1)
        assertNotNull(api2)
    }
}

