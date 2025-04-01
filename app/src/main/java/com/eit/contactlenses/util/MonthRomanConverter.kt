package com.eit.contactlenses.util

fun getRomanNumeralForMonth(montIndex: Int): String {
    return when (montIndex){
        0 -> "I"
        1 -> "II"
        2 -> "III"
        3 -> "IV"
        4 -> "V"
        5 -> "VI"
        6 -> "VII"
        7 -> "VIII"
        8 -> "IX"
        9 -> "X"
        10 -> "XI"
        11 -> "XII"
        else -> ""
    }
}