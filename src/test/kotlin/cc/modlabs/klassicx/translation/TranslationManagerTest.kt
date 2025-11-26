package cc.modlabs.klassicx.translation

import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import cc.modlabs.klassicx.translation.live.HelloEvent
import cc.modlabs.klassicx.translation.live.KeyCreatedEvent
import cc.modlabs.klassicx.translation.live.KeyDeletedEvent
import cc.modlabs.klassicx.translation.live.KeyUpdatedEvent
import cc.modlabs.klassicx.translation.live.LiveUpdateEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeTranslationSource(
    private val translationId: String = "test",
    initialLanguages: List<String> = listOf("en_US"),
) : TranslationSource {
    private val langs = initialLanguages.toMutableList()

    // backing store: lang -> key -> value
    val store: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

    // shared flow to simulate live updates
    private val updates = MutableSharedFlow<LiveUpdateEvent>(extraBufferCapacity = 64)

    override suspend fun getLanguages(): List<String> = langs.toList()

    override suspend fun getTranslations(language: String): List<Translation> =
        store[language].orEmpty().map { (k, v) -> Translation(language, k, v) }

    override fun liveUpdates(): Flow<LiveUpdateEvent> = updates

    suspend fun emit(event: LiveUpdateEvent) { updates.emit(event) }

    fun put(lang: String, key: String, value: String) {
        store.getOrPut(lang) { mutableMapOf() }[key] = value
        if (lang !in langs) langs.add(lang)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationManagerTest {

    private suspend fun assertEventuallyEquals(
        expected: String,
        supplier: suspend () -> String?,
        timeoutMs: Long = 1500,
        stepMs: Long = 25,
    ) {
        val start = System.nanoTime()
        var last: String? = null
        while ((System.nanoTime() - start) / 1_000_000 < timeoutMs) {
            last = supplier()
            if (last == expected) {
                assertEquals(expected, last)
                return
            }
            delay(stepMs)
        }
        assertEquals(expected, last)
    }

    @Test
    fun load_and_get_with_placeholders() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello %name%!")
        val manager = TranslationManager(src)

        manager.loadTranslations()
        advanceUntilIdle()

        val t = manager.get("en_US", "greet", mapOf("name" to "World"))
        assertNotNull(t)
        assertEquals("Hello World!", t.message)
    }

    @Test
    fun fallback_to_en_US_when_missing_language() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello")
        val manager = TranslationManager(src)

        manager.loadTranslations()
        advanceUntilIdle()

        val t = manager.get("de_DE", "greet")
        assertNotNull(t)
        assertEquals("Hello", t.message)
    }

    @Test
    fun contains_reports_loaded_language() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "a", "A")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()
        assertTrue(manager.contains("en_US"))
    }

    @Test
    fun returns_null_for_unknown_key_in_fallback_language() = runTest {
        val src = FakeTranslationSource()
        // no keys at all
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()
        val t = manager.get("en_US", "missing")
        assertNull(t)
    }

    @Test
    fun live_update_key_updated_refreshes_single_locale() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello A")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        // verify baseline
        assertEquals("Hello A", manager.get("en_US", "greet")!!.message)

        // update backing store and emit key_updated
        src.put("en_US", "greet", "Hello B")
        src.emit(
            KeyUpdatedEvent(
                translationId = "test",
                keyId = "id-1",
                locale = "en_US",
                value = "Hello B",
                ts = "2025-01-01T00:00:00Z"
            )
        )

        // allow collector to refresh (uses Default dispatcher, so poll)
        assertEventuallyEquals(expected = "Hello B", supplier = { manager.get("en_US", "greet")?.message })
    }

    @Test
    fun live_update_key_created_refreshes_all_cached_locales() = runTest {
        val src = FakeTranslationSource(initialLanguages = listOf("en_US", "de_DE"))
        src.put("en_US", "only_en", "EN")
        src.put("de_DE", "only_de", "DE")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        // now add a new key to both languages in the store
        src.put("en_US", "new_key", "VALUE_EN")
        src.put("de_DE", "new_key", "WERT_DE")

        // emit key_created to trigger bulk refresh
        src.emit(
            KeyCreatedEvent(
                translationId = "test",
                keyId = "kid-1",
                key = "new_key",
                ts = "2025-01-01T00:00:01Z"
            )
        )

        // allow collector to refresh (uses Default dispatcher, so poll)
        assertEventuallyEquals(expected = "VALUE_EN", supplier = { manager.get("en_US", "new_key")?.message })
        assertEventuallyEquals(expected = "WERT_DE", supplier = { manager.get("de_DE", "new_key")?.message })
    }
}
