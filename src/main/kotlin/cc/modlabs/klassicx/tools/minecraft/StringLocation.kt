package cc.modlabs.klassicx.tools.minecraft

data class StringLocation(
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val world: String,
) {

    override fun toString(): String {
        return "${x},${y},${z},${yaw},${pitch},${world}"
    }

    fun toBlockString(): String {
        return "${x.toInt()},${y.toInt()},${z.toInt()}"
    }

}

fun String.toStringLocation(): StringLocation {
    val split = split(",")
    return StringLocation(
        split.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
        split.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
        split.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
        split.getOrNull(3)?.toFloatOrNull() ?: 0.0f,
        split.getOrNull(4)?.toFloatOrNull() ?: 0.0f,
        split.getOrNull(5) ?: "world",
    )
}