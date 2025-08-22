package com.example.poker.util

import java.text.DecimalFormat
import java.util.Locale

fun Long.toBB(bigBlind: Long): String {
    if (bigBlind <= 0) return this.toString()
    val bbValue = this.toDouble() / bigBlind.toDouble()
    // Форматируем до одного знака после запятой
    return DecimalFormat("#.#").format(bbValue)
}

fun Long.toBBFloat(bigBlind: Long): Float {
    if (bigBlind <= 0) return this.toFloat()
    val bbValue = this.toDouble() / bigBlind.toDouble()
    return bbValue.toFloat()
}

fun Long.toMinutesSeconds(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.ROOT, "%d:%02d", minutes, seconds)
}