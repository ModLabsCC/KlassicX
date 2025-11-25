package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.wrappers.transForge.TransForgeException
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TransForgeExceptionTest {

    @Test
    fun testExceptionCreation() {
        val exception = TransForgeException("Test error", 404, "Not found")
        
        assertEquals("Test error", exception.message)
        assertEquals(404, exception.statusCode)
        assertEquals("Not found", exception.responseBody)
    }

    @Test
    fun testExceptionWithoutResponseBody() {
        val exception = TransForgeException("Test error", 500)
        
        assertEquals("Test error", exception.message)
        assertEquals(500, exception.statusCode)
        assertNull(exception.responseBody)
    }
}

