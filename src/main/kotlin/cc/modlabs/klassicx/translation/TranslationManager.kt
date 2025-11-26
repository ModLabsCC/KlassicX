package cc.modlabs.klassicx.translation

import cc.modlabs.klassicx.extensions.getInternalKlassicxLogger
import cc.modlabs.klassicx.tools.TempStorage
import cc.modlabs.klassicx.translation.interfaces.LiveUpdateCallback
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import cc.modlabs.klassicx.translation.live.HelloEvent
import cc.modlabs.klassicx.translation.live.KeyCreatedEvent
import cc.modlabs.klassicx.translation.live.KeyDeletedEvent
import cc.modlabs.klassicx.translation.live.KeyUpdatedEvent
import cc.modlabs.klassicx.translation.live.LiveUpdateEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock

class TranslationManager(
    private val source: TranslationSource
) {

    /**
     * The fallback language to be used when a translation is not found for a given language code.
     * The fallback language is represented by a dash-combined ISO-639 (language) and ISO-3166 (country) code.
     * If no translation is found for the specified language code, the system will attempt to find the translation in the fallback language.
     */
    private val fallbackLanguage = "en_US"

    /**
     * This variable represents a ReadWriteLock object named 'lock'. It is used for managing concurrent access to shared resources.
     * The ReadWriteLock allows multiple threads to read the resource concurrently, but only one thread can write to the resource at a time.
     */
    private val lock: ReadWriteLock = ReentrantReadWriteLock()

    /**
     * Represents a cache for storing translations.
     *
     * The cache is a map where the language code is the key and a list of translations is the value.
     *
     * @property cache The map representing the cache. It is initially empty.
     */
    private var cache: Map<String, List<Translation>> = emptyMap()

    private val notFoundTranslations = mutableListOf<String>()

    // Coroutine machinery for optional live updates from the source
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var liveJob: Job? = null
    private val liveMutex = Mutex()

    /**
     * List of registered callbacks for live update events.
     * Callbacks are invoked when a live update event is received and processed.
     * Using CopyOnWriteArrayList for thread-safe iteration during notification.
     */
    private val liveUpdateCallbacks = java.util.concurrent.CopyOnWriteArrayList<LiveUpdateCallback>()

    init {
        // Attempt to start live updates collection if the source supports it
        // This is best-effort and will no-op if live updates are not available.
        scope.launch { ensureLiveUpdatesStarted() }
    }

    /**
     * Registers a callback to be notified when live update events are received.
     * 
     * The callback will be invoked for all live update events including:
     * - HelloEvent - when the connection is established
     * - KeyCreatedEvent - when a new translation key is created
     * - KeyDeletedEvent - when a translation key is deleted
     * - KeyUpdatedEvent - when a translation value is updated
     * 
     * @param callback The callback to register
     */
    fun registerLiveUpdateCallback(callback: LiveUpdateCallback) {
        liveUpdateCallbacks.add(callback)
    }

    /**
     * Unregisters a previously registered live update callback.
     * 
     * @param callback The callback to unregister
     * @return true if the callback was found and removed, false otherwise
     */
    fun unregisterLiveUpdateCallback(callback: LiveUpdateCallback): Boolean {
        return liveUpdateCallbacks.remove(callback)
    }

    /**
     * Notifies all registered callbacks about a live update event.
     * 
     * @param event The event to notify callbacks about
     */
    private fun notifyLiveUpdateCallbacks(event: LiveUpdateEvent) {
        for (callback in liveUpdateCallbacks) {
            try {
                callback.onLiveUpdate(event)
            } catch (t: Throwable) {
                getInternalKlassicxLogger().error("Error in live update callback", t)
            }
        }
    }

    /**
     * Retrieves all translations from the cache.
     *
     * @return A map containing language code as the key and the number of translations as the value.
     */
    private fun getAll(): Map<String, Int> {
        try {
            lock.readLock().lock()
            return cache.mapValues { it.value.size }
        } finally {
            lock.readLock().unlock()
        }
    }

    fun getAllTranslations(): Map<String, List<Translation>> {
        return cache
    }

    suspend fun loadTranslations(callback: ((Map<String, Int>) -> Unit)? = null) {
        getInternalKlassicxLogger().info("Retrieving all translations from backend")
        val all = source.getLanguages()

        // Clear the cache before (re)loading
        cache = emptyMap()

        // Load translations in the caller's coroutine context so tests using runTest/advanceUntilIdle
        // can deterministically wait for completion. We still parallelize per-language loads.
        kotlinx.coroutines.coroutineScope {
            for (languageCode in all) {
                launch {
                    loadTranslationsForLanguage(languageCode)
                }
            }
        }

        // Invoke callback after all languages have been loaded
        callback?.invoke(getAll())

        // Ensure we are subscribed to live updates after an initial load is completed
        ensureLiveUpdatesStarted()
    }

    private suspend fun loadTranslationsForLanguage(languageCode: String) {
        val all = source.getTranslations(languageCode)

        val previousTranslations = cache[languageCode] ?: emptyList()
        put(languageCode, (previousTranslations + all.map { translation ->
            Translation(languageCode, translation.messageKey, translation.message)
        }))
    }

    /**
     * Retrieves the translation for the given language code and message key.
     *
     * @param languageCode The language code of the translation as dash-combined ISO-639 (language) and ISO-3166 (country).
     * @param messageKey The key identifying the message.
     * @param placeholders The placeholders to be replaced in the message.
     * @return The translation matching the language code and message key, or null if not found.
     */
    fun get(languageCode: String, messageKey: String, placeholders: Map<String, Any?> = emptyMap()): Translation? {
        try {
            lock.readLock().lock()
            var message = cache[languageCode]?.find { it.messageKey == messageKey }
                ?: cache[fallbackLanguage]?.find { it.messageKey == messageKey }

            if (message == null) {
                getInternalKlassicxLogger().info("No translation found for $languageCode:$messageKey")
                if (languageCode != fallbackLanguage) return null
                notFoundTranslations.add(
                    messageKey + "||" + placeholders.toList()
                        .joinToString("|") { "${it.first}::${it.second?.let { it::class.java.simpleName } ?: "null"}" })
                writeFailedTranslations()
                return null
            }

            for ((key, value) in placeholders) {
                message = message?.copy(message = message.message.replace("%$key%", value.toString()))
            }

            return message
        } finally {
            lock.readLock().unlock()
        }
    }

    private fun writeFailedTranslations() {
        if (notFoundTranslations.isEmpty()) return
        TempStorage.saveTempFile("not-found-translations.txt", notFoundTranslations.distinct().joinToString("\n"))
    }

    /**
     * Adds a translation to the cache for a given name.
     *
     * @param name The name of the translation.
     * @param translation The list of Translation objects representing the translations of a message in different languages.
     */
    private fun put(name: String, translation: List<Translation>) {
        try {
            lock.writeLock().lock()
            cache = cache + (name to translation)
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Replace translations for a language in the cache atomically.
     */
    private fun setLanguage(name: String, translations: List<Translation>) {
        try {
            lock.writeLock().lock()
            cache = cache + (name to translations)
        } finally {
            lock.writeLock().unlock()
        }
    }

    /**
     * Start collecting live updates if the source provides a Flow.
     * Safe to call many times; only starts once.
     */
    private suspend fun ensureLiveUpdatesStarted() {
        liveMutex.withLock {
            if (liveJob != null) return
            val flow = source.liveUpdates() ?: run {
                getInternalKlassicxLogger().info("Translation source does not provide live updates; continuing without WS.")
                return
            }
            getInternalKlassicxLogger().info("Starting live translation updates listener…")
            liveJob = scope.launch {
                try {
                    flow.collect { evt ->
                        // Notify all registered callbacks about the event
                        notifyLiveUpdateCallbacks(evt)
                        
                        when (evt) {
                            is HelloEvent -> {
                                getInternalKlassicxLogger().info("LiveUpdates connected for translation ${evt.translationId} with permission ${evt.permission}")
                            }
                            is KeyUpdatedEvent -> {
                                // Refresh only the affected locale
                                try {
                                    val language = evt.locale
                                    getInternalKlassicxLogger().info("LiveUpdates: key_updated -> refreshing locale '$language'")
                                    val fresh = source.getTranslations(language)
                                    setLanguage(language, fresh)
                                } catch (t: Throwable) {
                                    getInternalKlassicxLogger().error("Failed to refresh locale after key_updated", t)
                                }
                            }
                            is KeyCreatedEvent, is KeyDeletedEvent -> {
                                // Key set changed; refresh all locales currently present in cache
                                try {
                                    val locales = try {
                                        lock.readLock().lock()
                                        cache.keys.toList()
                                    } finally {
                                        lock.readLock().unlock()
                                    }
                                    if (locales.isEmpty()) return@collect
                                    getInternalKlassicxLogger().info("LiveUpdates: ${evt.type} -> refreshing locales ${locales.joinToString()}")
                                    locales.forEach { lang ->
                                        try {
                                            val fresh = source.getTranslations(lang)
                                            setLanguage(lang, fresh)
                                        } catch (inner: Throwable) {
                                            getInternalKlassicxLogger().error("Failed to refresh locale '$lang' after ${evt.type}", inner)
                                        }
                                    }
                                } catch (t: Throwable) {
                                    getInternalKlassicxLogger().error("Failed bulk refresh after ${evt.type}", t)
                                }
                            }
                        }
                    }
                } catch (t: Throwable) {
                    getInternalKlassicxLogger().error("Live updates listener terminated with error", t)
                }
            }
        }
    }

    /**
     * Checks if the given name exists in the cache.
     *
     * @param name the name to check for existence in the cache
     * @return true if the name exists in the cache, false otherwise
     */
    fun contains(name: String): Boolean {
        try {
            lock.readLock().lock()
            return cache.containsKey(name)
        } finally {
            lock.readLock().unlock()
        }
    }
}