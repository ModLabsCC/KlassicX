package cc.modlabs.klassicx.translation.interfaces

import cc.modlabs.klassicx.translation.Translation

interface TranslationSource {
    suspend fun getLanguages(): List<String>

    suspend fun getTranslations(language: String): List<Translation>
}