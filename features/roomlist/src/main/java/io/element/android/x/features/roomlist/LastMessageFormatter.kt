package io.element.android.x.features.roomlist

import android.text.format.DateFormat
import android.text.format.DateUtils
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.absoluteValue

class LastMessageFormatter(
    private val clock: Clock = Clock.System,
    private val locale: Locale = Locale.getDefault()
) {

    private val onlyTimeFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "HH:mm")
        DateTimeFormatter.ofPattern(pattern)
    }

    private val dateWithMonthFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "d MMM")
        DateTimeFormatter.ofPattern(pattern)
    }

    private val dateWithYearFormatter: DateTimeFormatter by lazy {
        val pattern = DateFormat.getBestDateTimePattern(locale, "dd.MM.yyyy")
        DateTimeFormatter.ofPattern(pattern)
    }


    fun format(timestamp: Long?): String {
        if (timestamp == null) return ""
        val now: Instant = clock.now()
        val tsInstant = Instant.fromEpochSeconds(timestamp)
        val nowDateTime = now.toLocalDateTime(TimeZone.currentSystemDefault())
        val tsDateTime = tsInstant.toLocalDateTime(TimeZone.currentSystemDefault())
        val isSameDay = nowDateTime.date == tsDateTime.date
        return when {
            isSameDay -> {
                onlyTimeFormatter.format(tsDateTime.toJavaLocalDateTime())
            }
            else -> {
                formatDate(tsDateTime, nowDateTime)
            }
        }
    }

    private fun formatDate(
        date: LocalDateTime,
        currentDate: LocalDateTime,
    ): String {
        val period = Period.between(date.date.toJavaLocalDate(), currentDate.date.toJavaLocalDate())
        return if (period.years.absoluteValue >= 1) {
            formatDateWithYear(date)
        } else if (period.days.absoluteValue < 2 && period.months.absoluteValue < 1) {
            getRelativeDay(date.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds())
        } else {
            formatDateWithMonth(date)
        }
    }

    private fun formatDateWithMonth(localDateTime: LocalDateTime): String {
        return dateWithMonthFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    private fun formatDateWithYear(localDateTime: LocalDateTime): String {
        return dateWithYearFormatter.format(localDateTime.toJavaLocalDateTime())
    }

    private fun getRelativeDay(ts: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            ts,
            clock.now().toEpochMilliseconds(),
            DateUtils.DAY_IN_MILLIS,
            DateUtils.FORMAT_SHOW_WEEKDAY
        ).toString()
    }


}