package com.github.dsulimchuk.dynamicquery.core

import java.util.*

class Test<in T>(val condition: (T) -> Boolean) {
    var macrosText: String = ""
    val queryHints = ArrayList<String>()

    operator fun String.unaryPlus() {
        macrosText += " and (${this.trim()})"
    }

    fun addQueryHint(hint: String) {
        queryHints += hint
    }
}