package cc.modlabs.klassicx.translation.sources

import cc.modlabs.klassicx.translation.Translation
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonTranslationSourceTest {

    @TempDir
    lateinit var tmp: File

    @Test
    fun discovers_languages_and_loads_translations() = runBlocking {
        // create en_US.json and de_DE.json
        File(tmp, "en_US.json").writeText("""{"greet":"Hello","bye":"Bye"}""")
        File(tmp, "de_DE.json").writeText("""{"greet":"Hallo"}""")

        val src = JsonTranslationSource(tmp)

        val langs = src.getLanguages().sorted()
        assertEquals(listOf("de_DE", "en_US"), langs)

        val en = src.getTranslations("en_US").associateBy(Translation::messageKey)
        val de = src.getTranslations("de_DE").associateBy(Translation::messageKey)

        assertEquals("Hello", en["greet"]?.message)
        assertEquals("Bye", en["bye"]?.message)
        assertEquals("Hallo", de["greet"]?.message)
        assertTrue(src.getTranslations("fr_FR").isEmpty())
    }
}
