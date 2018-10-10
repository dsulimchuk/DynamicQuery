package com.github.dsulimchuk.dynamicquery.core

class Test<in T>(val condition: (T) -> Boolean) {
    private var counter: Int = 0
    private var macrosText: String = ""

    fun text() = macrosText

    operator fun String.unaryPlus() {
        if (this.isEmpty()) return
        macrosText += if (counter++ > 0) " $this" else this
    }
}