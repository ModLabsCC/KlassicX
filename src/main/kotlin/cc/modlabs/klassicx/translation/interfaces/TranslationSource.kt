package cc.modlabs.klassicx.translation.interfaces

import cc.modlabs.klassicx.translation.Translation
import cc.modlabs.klassicx.translation.live.LiveUpdateEvent
import kotlinx.coroutines.flow.Flow

interface TranslationSource {
    suspend fun getLanguages(): List<String>

    suspend fun getTranslations(language: String): List<Translation>

    /**
     * Optional live updates stream. Implementations may return a Flow of [LiveUpdateEvent]
     * if they support real-time updates via WebSocket or other push mechanisms.
     *
     * Default is null (no live updates available).
     */
    fun liveUpdates(): Flow<LiveUpdateEvent>? = null
}