package cc.modlabs.klassicx.translation

import cc.modlabs.klassicx.translation.interfaces.TranslationHook
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

private class SimpleSource : TranslationSource {
    override suspend fun getLanguages(): List<String> = listOf("en_US")
    override suspend fun getTranslations(language: String): List<Translation> =
        listOf(Translation(language, "hello", "Hello"))
}

class TranslationsTest {

    @Test
    fun load_and_getTranslation_with_hook_applied() = runBlocking {
        // Install a simple hook that appends an exclamation mark
        Translations.registerTranslationHook(TranslationHook { _, _, _, result -> "$result!" })

        Translations.load(SimpleSource())

        // Wait until the translation is available (load is async on Default dispatcher)
        var value: String? = null
        repeat(60) { // up to ~1.2s
            value = Translations.getTranslation("en_US", "hello")
            if (value != null) return@repeat
            delay(20)
        }

        assertNotNull(value)
        assertEquals("Hello!", value)
    }
}
