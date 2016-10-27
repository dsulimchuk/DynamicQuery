package com.ds.query

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

/**
 * @author Dmitrii Sulimchuk
 * *         created 27/10/16
 */
@RunWith(Parameterized::class)
class AbstractDialectQueryParametersTest(
        val query: String,
        val expectedCount: Int,
        val expectedParams: List<String>) : AbstractDialect() {

    companion object {
        @Parameters(name = "{index}: fib({0})={1}")
        @JvmStatic
        fun data(): Collection<Array<Any>> {
            return listOf(
                    arrayOf("", 0, emptyList<Any>()),
                    arrayOf("select 1 from dual where 1 = 1", 0, emptyList<Any>()),
                    arrayOf("select 1 from dual where 1 = :a", 1, listOf("a")),
                    arrayOf("select 1 from dual where 1 = :a and 2 = :b", 2, listOf("a", "b")),
                    arrayOf("select 1 from dual where 1 = upper(:a) and 2 = :b", 2, listOf("a", "b")),
                    arrayOf("select 1 from dual where 1 = upper(:a||'%') and 2 = :b", 2, listOf("a", "b")),
                    arrayOf("select 1 from dual where 1 = upper(:a.b.c||'%')", 1, listOf("a.b.c"))
            )
        }
    }

    @Test
    fun testFindAllQueryParameters() {
        val result = findAllQueryParameters(query)

        assertThat(result, notNullValue())
        assertThat("size equals", result.size, equalTo(expectedCount))
        assertThat("check that result is unique",
                result.distinct().size, equalTo(result.size))
        assertTrue("chech expected params ${expectedParams} in ${result}",
                result.containsAll(expectedParams))
    }

}