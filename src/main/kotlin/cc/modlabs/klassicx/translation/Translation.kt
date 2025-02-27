package cc.modlabs.klassicx.translation

import kotlinx.serialization.Serializable

@Serializable
data class Translation(
    val languageCode: String = "en_US",
    val messageKey: String,
    val message: String,
) {

    override fun toString(): String {
        return message
    }

}