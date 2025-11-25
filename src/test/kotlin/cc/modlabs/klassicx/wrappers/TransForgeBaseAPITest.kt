package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.TransForgeBaseAPI
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.net.http.HttpClient
import java.net.http.HttpResponse

class TransForgeBaseAPITest {

    @Test
    fun testNormalizeBaseUrl() {
        val api = TestBaseAPI("https://example.com/", HttpClient.newHttpClient())
        assertEquals("https://example.com", api.testNormalizeBaseUrl("https://example.com/"))
        assertEquals("https://example.com", api.testNormalizeBaseUrl("https://example.com"))
    }

    @Test
    fun testBuildUrl() {
        val api = TestBaseAPI("https://example.com", HttpClient.newHttpClient())
        assertEquals("https://example.com/api/test", api.testBuildUrl("/api/test"))
        assertEquals("https://example.com/api/test", api.testBuildUrl("api/test"))
    }

    // Test helper class to access protected methods
    private class TestBaseAPI(baseUrl: String, httpClient: HttpClient) : TransForgeBaseAPI(baseUrl, httpClient) {
        fun testNormalizeBaseUrl(url: String) = normalizeBaseUrl(url)
        fun testBuildUrl(path: String) = buildUrl(path)
    }
}

