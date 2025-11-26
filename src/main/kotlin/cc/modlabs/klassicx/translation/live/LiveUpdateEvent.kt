package cc.modlabs.klassicx.translation.live

/**
 * Sealed hierarchy describing live update events pushed over the WebSocket.
 *
 * wireName values are stable and match the server contract:
 *  - "hello"
 *  - "key_created"
 *  - "key_deleted"
 *  - "key_updated"
 */
sealed interface LiveUpdateEvent {
    val type: String
    val translationId: String
}

data class HelloEvent(
    override val translationId: String,
    val permission: String,
) : LiveUpdateEvent {
    override val type: String = "hello"
}

data class KeyCreatedEvent(
    override val translationId: String,
    val keyId: String,
    val key: String,
    val ts: String,
) : LiveUpdateEvent {
    override val type: String = "key_created"
}

data class KeyDeletedEvent(
    override val translationId: String,
    val keyId: String,
    val ts: String,
) : LiveUpdateEvent {
    override val type: String = "key_deleted"
}

data class KeyUpdatedEvent(
    override val translationId: String,
    val keyId: String,
    val locale: String,
    val value: String?,
    val ts: String,
) : LiveUpdateEvent {
    override val type: String = "key_updated"
}
