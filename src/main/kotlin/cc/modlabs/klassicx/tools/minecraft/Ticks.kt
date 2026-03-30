package cc.modlabs.klassicx.tools.minecraft

import kotlin.time.Duration

val Duration.inMinecraftTicks: Long
    get() = this.inWholeMilliseconds / 50

val java.time.Duration.inMinecraftTicks: Long
    get() = this.toMillis() / 50