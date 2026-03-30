package cc.modlabs.klassicx.extensions

import cc.modlabs.klassicx.tools.random.RandomTagType
import cc.modlabs.klassicx.tools.random.randomTag
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExceptionsTest {

    @Test
    fun catchException_passes_generated_tag_to_handler() {
        var handledTag: String? = null

        catchException(
            exception = IllegalStateException("boom"),
            catch = ExceptionCatch { _, tag -> handledTag = tag },
            random = Random(1234),
        )

        assertNotNull(handledTag)
        assertTrue(handledTag!!.startsWith("#"))
        assertEquals(6, handledTag!!.length)
    }

    @Test
    fun tryWithResult_returns_failure_and_invokes_handler() {
        var handled = false

        val result = tryWithResult(
            catch = ExceptionCatch<Exception> { throwable, tag ->
                handled = true
                assertTrue(throwable is IllegalArgumentException)
                assertEquals("", tag)
            }
        ) {
            throw IllegalArgumentException("bad")
        }

        assertTrue(handled)
        assertTrue(result.isFailure)
    }

    @Test
    fun tryOrElse_returns_fallback_value() {
        val value = tryOrElse(other = 42) {
            error("fail")
        }

        assertEquals(42, value)
    }

    @Test
    fun tryOrNull_returns_null_on_failure() {
        val value = tryOrNull<String> {
            error("fail")
        }

        assertNull(value)
    }

    @Test
    fun randomTag_supports_lowercase_without_prefix() {
        val tag = randomTag(
            size = 8,
            prefix = null,
            case = RandomTagType.ONLY_LOWERCASE,
            randomizer = Random(99),
        )

        assertEquals(8, tag.length)
        assertTrue(tag.all { it.isLowerCase() || it.isDigit() })
    }
}
