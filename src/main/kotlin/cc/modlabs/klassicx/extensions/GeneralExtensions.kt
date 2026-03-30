package cc.modlabs.klassicx.extensions

inline fun <T> all(vararg objects: T, check: (T) -> Boolean): Boolean = objects.all(check)

inline fun <T> none(vararg objects: T, check: (T) -> Boolean): Boolean = objects.none(check)

inline fun <T> any(vararg objects: T, check: (T) -> Boolean): Boolean = objects.any(check)

inline fun <T> T.modifiedIf(modifyIf: Boolean, modification: (T) -> T): T {
    return if (modifyIf) modification(this) else this
}

inline fun <T> T.modifyIf(modifyIf: Boolean, modification: T.() -> Unit): T {
    return if (modifyIf) apply(modification) else this
}

inline fun <T> T.modifiedIfSuccess(modification: (T) -> T): T {
    return runCatching { modification(this) }.getOrElse { this }
}

val Any?.isNull: Boolean
    get() = this == null

val Any?.isNotNull: Boolean
    get() = this != null

@Suppress("EmptyMethod")
fun empty() {}

inline fun <T> T?.applyIfNull(process: (T?) -> Unit): T? {
    if (this == null) {
        process(this)
    }
    return this
}

inline fun <T> T?.applyIfNotNull(process: (T & Any) -> Unit): T? {
    if (this != null) {
        process(this)
    }
    return this
}

inline fun <T> T?.ifNull(process: () -> Unit) {
    if (this == null) {
        process()
    }
}

inline fun <T> T?.ifNotNull(process: () -> Unit) {
    if (this != null) {
        process()
    }
}

fun <T, D> Pair<T?, D>.asDefaultNullDodge(): Any? {
    return if (first != null) first else second
}
