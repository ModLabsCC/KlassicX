package cc.modlabs.klassicx.extensions

import cc.modlabs.klassicx.tools.random.randomTag
import java.util.logging.Logger
import kotlin.random.Random

fun interface ExceptionCatch<E : Throwable> {

    fun handleException(throwable: E, tag: String)

    operator fun invoke(throwable: E, tag: String) = handleException(throwable, tag)

    companion object {
        fun <E : Throwable> ignore(): ExceptionCatch<E> = ExceptionCatch { _, _ -> }

        fun <E : Throwable> log(): ExceptionCatch<E> = ExceptionCatch { error, tag ->
            println(formatExceptionMessage(error, tag))
        }

        fun <E : Throwable> log(logger: Logger): ExceptionCatch<E> = ExceptionCatch { error, tag ->
            logger.severe(formatExceptionMessage(error, tag))
        }

        private fun formatExceptionMessage(error: Throwable, tag: String): String {
            val suffix = error.message?.takeIf { it.isNotBlank() } ?: error::class.simpleName.orEmpty()
            return "Exception${tag.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""}: $suffix"
        }
    }
}

private fun Exception.traceLabel(): String {
    return stackTrace.firstOrNull()?.className
        ?: this::class.qualifiedName
        ?: "Unknown exception"
}

private fun printTaggedException(exception: Exception, tag: String) {
    val label = exception.traceLabel()
    println(" > $tag - $label")
    exception.printStackTrace()
    println(" < $tag - $label")
}

fun <T : Exception> catchException(
    exception: T,
    catch: ExceptionCatch<T> = ExceptionCatch.ignore(),
    random: Random = Random,
) {
    val tag = randomTag(randomizer = random)
    catch(exception, tag)
    printTaggedException(exception, tag)
}

inline fun <T> tryWithResult(
    silent: Boolean = true,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> T,
): Result<T> = try {
    Result.success(process())
} catch (exception: Exception) {
    if (silent) {
        catch(exception, "")
    } else {
        catchException(exception, catch)
    }
    Result.failure(exception)
}

inline fun <T> tryWithResult(
    silent: () -> Boolean,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> T,
): Result<T> = tryWithResult(silent = silent(), catch = catch, process = process)

inline fun <R, T : R> tryOrElse(
    silent: Boolean = true,
    other: T,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> R,
): R = tryWithResult(silent = silent, catch = catch, process = process).getOrElse { other }

inline fun <R, T : R> tryOrElse(
    silent: () -> Boolean,
    other: T,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> R,
): R = tryOrElse(silent = silent(), other = other, catch = catch, process = process)

inline fun <T> tryOrNull(
    silent: Boolean = true,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> T,
): T? = tryWithResult(silent = silent, catch = catch, process = process).getOrNull()

inline fun <T> tryOrNull(
    silent: () -> Boolean,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> T,
): T? = tryOrNull(silent = silent(), catch = catch, process = process)

inline fun <T> tryCatch(
    catch: (Exception) -> T,
    process: () -> T,
): T = try {
    process()
} catch (exception: Exception) {
    catch(exception)
}

inline fun <T> tryCatch(
    silent: Boolean = true,
    exceptionCatch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    catch: (Exception) -> T,
    process: () -> T,
): T = try {
    process()
} catch (exception: Exception) {
    if (silent) {
        exceptionCatch(exception, "")
    } else {
        catchException(exception, exceptionCatch)
    }
    catch(exception)
}

inline fun tryCatch(
    silent: Boolean = true,
    exceptionCatch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    catch: (Exception) -> Unit,
    process: () -> Unit,
) {
    try {
        process()
    } catch (exception: Exception) {
        if (silent) {
            exceptionCatch(exception, "")
        } else {
            catchException(exception, exceptionCatch)
        }
        catch(exception)
    }
}

inline fun tryOrIgnore(
    silent: Boolean = true,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> Unit,
) {
    tryWithResult(silent = silent, catch = catch, process = process)
}

inline fun tryOrIgnore(
    silent: () -> Boolean,
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> Unit,
) = tryOrIgnore(silent = silent(), catch = catch, process = process)

inline fun tryOrPrint(
    catch: ExceptionCatch<Exception> = ExceptionCatch.ignore(),
    process: () -> Unit,
) = tryOrIgnore(silent = false, catch = catch, process = process)
