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

fun <T> Iterable<T>.getOrNull(index: Int): T? {
    if (index < 0) {
        return null
    }

    return when (this) {
        is List<T> -> this.getOrNull(index)
        else -> this.drop(index).firstOrNull()
    }
}

inline fun <T> Iterable<T>.getOrElse(index: Int, defaultValue: (Int) -> T): T {
    return getOrNull(index) ?: defaultValue(index)
}

operator fun <T> Iterable<T>.get(index: Int): T {
    return getOrNull(index) ?: throw IndexOutOfBoundsException("Index $index out of bounds for iterable")
}

fun <T> MutableList<T>.setOrNull(index: Int, value: T): T? {
    if (index !in indices) {
        return null
    }

    return set(index, value)
}

fun <T> MutableList<T>.setIfPresent(index: Int, value: T): Boolean {
    return if (index in indices) {
        this[index] = value
        true
    } else {
        false
    }
}

fun <T> MutableList<T>.setOrAdd(index: Int, value: T): MutableList<T> {
    require(index >= 0) { "Index must be non-negative" }

    when {
        index < size -> this[index] = value
        index == size -> add(value)
        else -> throw IndexOutOfBoundsException("Index $index out of bounds for mutable list of size $size")
    }

    return this
}

fun <K, V> MutableMap<K, V>.getOrSet(key: K, defaultValue: () -> V): V {
    return getOrPut(key, defaultValue)
}
