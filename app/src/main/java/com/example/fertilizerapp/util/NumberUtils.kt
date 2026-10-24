package com.example.fertilizerapp.util

import java.util.Locale

/** Parses user input, accepting both "1.5" and the German "1,5". */
fun String.toDecimalOrNull(): Double? = trim().replace(',', '.').toDoubleOrNull()

/** Formats a number without trailing zeros, e.g. 5.0 -> "5", 2.50 -> "2.5". */
fun Double.formatCompact(maxDecimals: Int = 2): String =
    String.format(Locale.ROOT, "%.${maxDecimals}f", this).trimEnd('0').trimEnd('.')
