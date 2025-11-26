package cc.modlabs.klassicx.translation.interfaces

import cc.modlabs.klassicx.translation.live.LiveUpdateEvent

/**
 * Callback interface for receiving translation live update events.
 * 
 * Applications can register callbacks to be notified when translation
 * changes are received via the live updates stream (WebSocket or similar).
 * 
 * This is useful for scenarios where the application needs to react to
 * translation changes, such as refreshing UI components or logging updates.
 */
fun interface LiveUpdateCallback {

    /**
     * Called when a live update event is received and processed.
     * 
     * @param event The live update event that was received. Can be one of:
     *   - [cc.modlabs.klassicx.translation.live.HelloEvent] - Initial connection acknowledgment
     *   - [cc.modlabs.klassicx.translation.live.KeyCreatedEvent] - A new translation key was created
     *   - [cc.modlabs.klassicx.translation.live.KeyDeletedEvent] - A translation key was deleted
     *   - [cc.modlabs.klassicx.translation.live.KeyUpdatedEvent] - A translation value was updated
     */
    fun onLiveUpdate(event: LiveUpdateEvent)

}
