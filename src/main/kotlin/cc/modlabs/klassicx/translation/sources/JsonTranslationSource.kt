package cc.modlabs.klassicx.translation.sources

import cc.modlabs.klassicx.translation.Translation
import cc.modlabs.klassicx.translation.interfaces.TranslationSource
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class JsonTranslationSource(private val directory: File) : TranslationSource {
    override suspend fun getLanguages(): List<String> {
        if (!directory.exists()) {
            directory.mkdirs()
        }
        return directory.listFiles { file -> file.extension == "json" }?.map { it.nameWithoutExtension } ?: emptyList()
    }

    override suspend fun getTranslations(language: String): List<Translation> {
        val langFile = File(directory, "$language.json")
        if (!langFile.exists()) return emptyList()

        val gson = Gson()
        val type = object : TypeToken<Map<String, String>>() {}.type
        // Ensure the reader is closed to avoid file locks on Windows during tests
        val data: Map<String, String> = langFile.bufferedReader().use { reader ->
            gson.fromJson(reader, type)
        }
        return data.map { (key, value) ->
            Translation(
                language,
                key,
                value
            )
        }
    }

}