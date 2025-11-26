package cc.modlabs.klassicx.translation

import cc.modlabs.klassicx.translation.interfaces.LiveUpdateCallback
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
import kotlin.test.assertFalse
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

    /**
     * Helper function to wait for a condition to become true within a timeout.
     * Useful for waiting for asynchronous events to be processed.
     */
    private suspend fun waitUntil(
        predicate: () -> Boolean,
        maxAttempts: Int = 60,
        delayMs: Long = 25L,
    ) {
        var attempts = 0
        while (!predicate() && attempts < maxAttempts) {
            delay(delayMs)
            attempts++
        }
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

    @Test
    fun live_update_callback_invoked_on_key_updated() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents = mutableListOf<LiveUpdateEvent>()
        manager.registerLiveUpdateCallback(LiveUpdateCallback { event ->
            receivedEvents.add(event)
        })

        val keyUpdatedEvent = KeyUpdatedEvent(
            translationId = "test",
            keyId = "id-1",
            locale = "en_US",
            value = "Hello Updated",
            ts = "2025-01-01T00:00:00Z"
        )
        src.emit(keyUpdatedEvent)

        // Wait for the event to be processed
        waitUntil(predicate = { receivedEvents.isNotEmpty() })

        assertEquals(1, receivedEvents.size)
        val event = receivedEvents[0]
        assertTrue(event is KeyUpdatedEvent)
        assertEquals("en_US", (event as KeyUpdatedEvent).locale)
    }

    @Test
    fun live_update_callback_invoked_on_hello_event() = runTest {
        val src = FakeTranslationSource()
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents = mutableListOf<LiveUpdateEvent>()
        manager.registerLiveUpdateCallback { event ->
            receivedEvents.add(event)
        }

        val helloEvent = HelloEvent(
            translationId = "test",
            permission = "READ"
        )
        src.emit(helloEvent)

        // Wait for the event to be processed
        waitUntil(predicate = { receivedEvents.isNotEmpty() })

        assertEquals(1, receivedEvents.size)
        assertTrue(receivedEvents[0] is HelloEvent)
        assertEquals("READ", (receivedEvents[0] as HelloEvent).permission)
    }

    @Test
    fun live_update_callback_invoked_on_key_created() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "existing", "Value")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents = mutableListOf<LiveUpdateEvent>()
        manager.registerLiveUpdateCallback { event ->
            receivedEvents.add(event)
        }

        val keyCreatedEvent = KeyCreatedEvent(
            translationId = "test",
            keyId = "new-key-id",
            key = "new_key",
            ts = "2025-01-01T00:00:00Z"
        )
        src.emit(keyCreatedEvent)

        // Wait for the event to be processed
        waitUntil(predicate = { receivedEvents.isNotEmpty() })

        assertEquals(1, receivedEvents.size)
        assertTrue(receivedEvents[0] is KeyCreatedEvent)
        assertEquals("new_key", (receivedEvents[0] as KeyCreatedEvent).key)
    }

    @Test
    fun live_update_callback_invoked_on_key_deleted() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "to_delete", "Value")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents = mutableListOf<LiveUpdateEvent>()
        manager.registerLiveUpdateCallback { event ->
            receivedEvents.add(event)
        }

        val keyDeletedEvent = KeyDeletedEvent(
            translationId = "test",
            keyId = "delete-key-id",
            ts = "2025-01-01T00:00:00Z"
        )
        src.emit(keyDeletedEvent)

        // Wait for the event to be processed
        waitUntil(predicate = { receivedEvents.isNotEmpty() })

        assertEquals(1, receivedEvents.size)
        assertTrue(receivedEvents[0] is KeyDeletedEvent)
    }

    @Test
    fun multiple_callbacks_all_receive_events() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents1 = mutableListOf<LiveUpdateEvent>()
        val receivedEvents2 = mutableListOf<LiveUpdateEvent>()

        manager.registerLiveUpdateCallback { event ->
            receivedEvents1.add(event)
        }
        manager.registerLiveUpdateCallback { event ->
            receivedEvents2.add(event)
        }

        src.emit(HelloEvent(translationId = "test", permission = "WRITE"))

        // Wait for events to be processed
        waitUntil(predicate = { receivedEvents1.isNotEmpty() && receivedEvents2.isNotEmpty() })

        assertEquals(1, receivedEvents1.size)
        assertEquals(1, receivedEvents2.size)
    }

    @Test
    fun unregister_callback_stops_notifications() = runTest {
        val src = FakeTranslationSource()
        src.put("en_US", "greet", "Hello")
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val receivedEvents = mutableListOf<LiveUpdateEvent>()
        val callback = LiveUpdateCallback { event ->
            receivedEvents.add(event)
        }

        manager.registerLiveUpdateCallback(callback)

        // Emit first event - should be received
        src.emit(HelloEvent(translationId = "test", permission = "READ"))
        
        // Use a longer timeout for this test as the live updates run on Default dispatcher
        waitUntil(predicate = { receivedEvents.isNotEmpty() }, maxAttempts = 120)
        assertEquals(1, receivedEvents.size)

        // Unregister the callback
        val removed = manager.unregisterLiveUpdateCallback(callback)
        assertTrue(removed)

        // Emit second event - should NOT be received
        src.emit(HelloEvent(translationId = "test", permission = "WRITE"))
        
        delay(200)
        assertEquals(1, receivedEvents.size) // Should still be 1
    }

    @Test
    fun unregister_nonexistent_callback_returns_false() = runTest {
        val src = FakeTranslationSource()
        val manager = TranslationManager(src)
        manager.loadTranslations()
        advanceUntilIdle()

        val callback = LiveUpdateCallback { }
        val removed = manager.unregisterLiveUpdateCallback(callback)
        assertFalse(removed)
    }
}
