package cc.modlabs.klassicx.wrappers

import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.SerialName
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
    val avatar: String,

    @SerialName("skin_texture")
    val skinTexture: String,
)