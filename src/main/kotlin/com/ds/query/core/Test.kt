package com.ds.query.core

class Test<in T>(val condition: (T) -> Boolean) {
    //lateinit var condition: (T) -> Boolean //= { true }
    var macrosText: String = ""

    operator fun String.unaryPlus() {
        macrosText += " and (${this.trim()})"
    }


}