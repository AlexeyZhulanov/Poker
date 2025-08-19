package com.example.poker.util

import java.text.DecimalFormat

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