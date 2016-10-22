package com.ds.query.core

import java.util.*

class Macros<T>(val name: String) {
    val testers = ArrayList<Test<T>>()

    fun test(condition: (T) -> Boolean, tester: Test<T>.() -> Unit): Test<T> {
        val test = Test(condition)
        test.tester()
        testers += test
        return test
    }
}