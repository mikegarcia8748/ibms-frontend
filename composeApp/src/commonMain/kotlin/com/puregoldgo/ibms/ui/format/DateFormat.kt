package com.puregoldgo.ibms.ui.format

/**
 * Turns an ISO date string from the server into something a person reads:
 * `"2024-01-15"` → `"Jan 15, 2024"`, `"2024-01-15T08:30:00Z"` → `"Jan 15, 2024"`.
 *
 * Handles both shapes the API sends: a bare `YYYY-MM-DD` (contract and
 * installation dates) and a full ISO-8601 timestamp (`createdAt`/`updatedAt`).
 * Only the calendar date is shown — these dialogs are read-only records where the
 * hour a row was written is noise.
 *
 * Hand-rolled, on the string, for the same reasons [formatMoney] is: Kotlin
 * Multiplatform has no locale date formatting in common code, and the UI module
 * does not carry `kotlinx-datetime`. Anything that does not begin with a valid
 * `YYYY-MM-DD` is returned untouched — a value that arrives malformed should
 * appear as the server sent it, not as a plausible-looking guess.
 *
 * Lives here rather than beside one console's mappers: every console that shows a
 * date needs it, and a second copy would be the one that formats differently.
 */
fun formatDate(raw: String?): String {
    val trimmed = raw?.trim().orEmpty()
    if (trimmed.isEmpty()) return trimmed

    // Take the calendar-date prefix, whether or not a time part follows.
    val datePart = trimmed.substringBefore('T').substringBefore(' ')
    val pieces = datePart.split('-')
    if (pieces.size != 3) return trimmed

    val year = pieces[0]
    val month = pieces[1].toIntOrNull()
    val day = pieces[2].toIntOrNull()

    if (year.length != 4 || !year.all { it.isDigit() }) return trimmed
    if (month == null || month !in 1..12) return trimmed
    if (day == null || day !in 1..31) return trimmed

    return "${MONTH_ABBREVIATIONS[month - 1]} $day, $year"
}

private val MONTH_ABBREVIATIONS = listOf(
    "Jan", "Feb", "Mar", "Apr", "May", "Jun",
    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
)
