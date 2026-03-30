package cc.modlabs.klassicx.extensions

import java.text.SimpleDateFormat
import java.time.Clock
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration
import kotlin.time.toKotlinDuration

fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration {
    var sum = Duration.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun calendarFromDateString(dateFormat: String): Calendar {
    val cal: Calendar = java.util.Calendar.getInstance()
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    cal.time = sdf.parse(dateFormat) // all done
    return cal
}

fun isWeekend(): Boolean {
    val now = LocalDateTime.now()
    val dayOfWeek = now.dayOfWeek
    val hour = now.hour

    return when (dayOfWeek) {
        DayOfWeek.FRIDAY -> hour >= 18
        DayOfWeek.SATURDAY -> true
        DayOfWeek.SUNDAY -> hour < 22
        else -> false
    }
}

fun Instant.durationToNow(clock: Clock = Clock.systemUTC()): Duration {
    return java.time.Duration.between(this, clock.instant()).toKotlinDuration()
}

fun Instant.durationFromNow(clock: Clock = Clock.systemUTC()): Duration {
    return java.time.Duration.between(clock.instant(), this).toKotlinDuration()
}

operator fun Instant.plus(duration: Duration): Instant {
    return plus(duration.inWholeNanoseconds, java.time.temporal.ChronoUnit.NANOS)
}

operator fun Instant.minus(duration: Duration): Instant {
    return minus(duration.inWholeNanoseconds, java.time.temporal.ChronoUnit.NANOS)
}

fun Duration.fromNow(clock: Clock = Clock.systemUTC()): Instant {
    return clock.instant().plus(this.inWholeNanoseconds, java.time.temporal.ChronoUnit.NANOS)
}

fun Duration.ago(clock: Clock = Clock.systemUTC()): Instant {
    return clock.instant().minus(this.inWholeNanoseconds, java.time.temporal.ChronoUnit.NANOS)
}


fun parseDurationStringToEpoch(durationString: String): Long? {
    if (durationString.isEmpty()) return null

    val regex = "(\\d+)([smhdw])".toRegex(RegexOption.IGNORE_CASE)
    val match = regex.matchEntire(durationString)

    if (match != null) {
        val (valueStr, unit) = match.destructured
        val value = valueStr.toLongOrNull() ?: return null

        if (value <= 0) return null

        val durationMillis = when (unit.lowercase()) {
            "s" -> java.util.concurrent.TimeUnit.SECONDS.toMillis(value)
            "m" -> java.util.concurrent.TimeUnit.MINUTES.toMillis(value)
            "h" -> java.util.concurrent.TimeUnit.HOURS.toMillis(value)
            "d" -> java.util.concurrent.TimeUnit.DAYS.toMillis(value)
            "w" -> java.util.concurrent.TimeUnit.DAYS.toMillis(value * 7)
            else -> return null
        }

        return System.currentTimeMillis() + durationMillis
    }

    return null
}
