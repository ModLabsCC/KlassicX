package cc.modlabs.klassicx.wrappers

import cc.modlabs.klassicx.extensions.getLogger
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import java.net.URI
import java.util.*

object MojangAPI {
    private val gson = Gson()

    fun getUser(user: String): MclSuccessResponse? {
        try {
            CoroutineScope(Dispatchers.Default).run {
                val url = "https://mcl.flawcra.cc/$user"
                val response = URI.create(url).toURL().readText()

                val errorResponse = gson.fromJson(response, MclErrorResponse::class.java)
                if (errorResponse.error != null) {
                    return null
                }

                return gson.fromJson(response, MclSuccessResponse::class.java)
            }
        } catch (e: Exception) {
            return null
        }
    }
}

fun usernameToRealUUID(input: String): UUID? {
    MojangAPI.getUser(input)?.let {
        return UUID.fromString(it.id)
    }
    return null
}

data class MclErrorResponse(
    val code: String? = null,
    val error: String? = null
)

data class MclSuccessResponse(
    val username: String,
    val id: String,

    @SerializedName("raw_id")
    val rawId: String,
    val avatar: String,

    @SerializedName("skin_texture")
    val skinTexture: String,

    @SerializedName("properties")
    val skinProperties: List<SkinProperties>,

    @SerializedName("mojang_skin_texture")
    val mojangSkinTexture: String
)

data class SkinProperties(
    val name: String,
    val value: String,
    val signature: String,
)