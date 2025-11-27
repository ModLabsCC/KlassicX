package cc.modlabs.klassicx.translation

import cc.modlabs.klassicx.translation.interfaces.LiveUpdateCallback
import cc.modlabs.klassicx.translation.interfaces.TranslationHook
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Translations {

    lateinit var manager: TranslationManager
    private val translationHooks = arrayListOf<TranslationHook>()

    fun registerTranslationHook(hook: TranslationHook) {
        translationHooks.add(hook)
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
     * @throws UninitializedPropertyAccessException if called before [load] has been called
     */
    fun registerLiveUpdateCallback(callback: LiveUpdateCallback) {
        manager.registerLiveUpdateCallback(callback)
    }

    /**
     * Unregisters a previously registered live update callback.
     * 
     * @param callback The callback to unregister
     * @return true if the callback was found and removed, false otherwise
     * @throws UninitializedPropertyAccessException if called before [load] has been called
     */
    fun unregisterLiveUpdateCallback(callback: LiveUpdateCallback): Boolean {
        return manager.unregisterLiveUpdateCallback(callback)
    }

    fun getTranslation(language: String, key: String, placeholders: Map<String, Any?> = mapOf()): String? {
        if (!Translations::manager.isInitialized) return null

        var translation = manager.get(language, key, placeholders)?.message ?: return null

        for (hook in translationHooks) {
            translation = hook.onHandleTranslation(language, key, placeholders, translation)
        }

        return translation
    }

    val translations get() = manager

    fun load(source: TranslationSource, callback: ((Map<String, Int>) -> Unit)? = null) {
        manager = TranslationManager(source)

        CoroutineScope(Dispatchers.Default).launch {
            manager.loadTranslations(callback)
        }
    }
}