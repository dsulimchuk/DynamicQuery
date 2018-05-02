package com.github.dsulimchuk.dynamicquery.core

class Test<in T>(val condition: (T) -> Boolean) {
    private var counter: Int = 0
    private var macrosText: String = ""

    fun text() = when (counter) {
        0, 1 -> macrosText
        else -> "($macrosText)"
    }

    operator fun String.unaryPlus() {
        if (this.isNotBlank() && counter++ > 0) macrosText += " and "
        macrosText += this
    }
}