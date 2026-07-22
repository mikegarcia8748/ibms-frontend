package com.puregoldgo.ibms.ui.format

/**
 * Groups a decimal string for display: `"2489.99"` → `"2,489.99"`.
 *
 * Hand-rolled because Kotlin Multiplatform has no `String.format` and no locale
 * number formatting in common code. It works on the string rather than parsing
 * to a number on purpose — this is a billing figure, and a round trip through
 * Double could change it.
 *
 * Anything that does not look like a decimal is returned untouched. A rate that
 * arrives malformed should appear as the server sent it, not silently reformatted
 * into something plausible.
 *
 * Lives here rather than beside one console's mappers: every console that shows
 * money needs it, and a second copy would be the one that rounds differently.
 */
fun formatMoney(raw: String): String {
    val trimmed = raw.trim()
    if (trimmed.isEmpty()) return trimmed

    val isNegative = trimmed.startsWith("-")
    val unsigned = trimmed.removePrefix("-")

    val dot = unsigned.indexOf('.')
    val whole = if (dot >= 0) unsigned.substring(0, dot) else unsigned
    val fraction = if (dot >= 0) unsigned.substring(dot + 1) else ""

    if (whole.isEmpty() || !whole.all { it.isDigit() }) return trimmed
    if (!fraction.all { it.isDigit() }) return trimmed

    val grouped = whole
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()

    return buildString {
        if (isNegative) append('-')
        append(grouped)
        append('.')
        append(fraction.ifEmpty { "00" })
    }
}
