package cc.modlabs.klassicx.extensions

import dev.fruxz.ascend.tool.time.TimeUnit
import dev.fruxz.ascend.tool.time.calendar.Calendar
import dev.fruxz.ascend.tool.time.clock.TimeDisplay
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.util.*
import kotlin.time.Duration

fun <T> Iterable<T>.sumOf(selector: (T) -> Duration): Duration {
    var sum = Duration.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

val Duration.betterString: String
    get() {
        return TimeDisplay(this).toClockString(TimeUnit.HOUR, TimeUnit.MINUTE, TimeUnit.SECOND)
    }

fun calendarFromDateString(dateFormat: String): Calendar {
    val cal: java.util.Calendar = java.util.Calendar.getInstance()
    val sdf = SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN)
    cal.time = sdf.parse(dateFormat) // all done
    return Calendar.fromLegacy(cal)
}

fun Calendar.formatToDay(locale: Locale): String {
    return SimpleDateFormat.getDateInstance(Calendar.FormatStyle.FULL.ordinal, locale).format(javaDate)
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

