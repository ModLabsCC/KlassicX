package cc.modlabs.klassicx.wrappers.transForge

/**
 * Exception thrown when a Transforge API request fails.
 */
class TransForgeException(
    message: String,
    val statusCode: Int,
    val responseBody: String? = null
) : Exception(message)

