package com.github.dsulimchuk.dynamicquery.core


class Test<in T>(val condition: (T) -> Boolean, val testAlias: String) {
    var macrosText: String = ""
    var innerQuery: QueryDsl<*>? = null

    operator fun String.unaryPlus() {
        macrosText += this.trim()
    }

    operator fun QueryDsl<*>.unaryPlus() {
        if (innerQuery != null) {
            throw RuntimeException("test already contains innerQuery")
        }
        innerQuery = this
        macrosText += this.prepareText().replace(Regex(":([^ ]+)"), ":${testAlias}#$1")
    }
}