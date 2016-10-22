package com.ds.query.core

import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * @author Dmitrii Sulimchuk
 * created 22/10/16
 */
class QueryTest {
    @Test
    fun m() {
        val query = Query("param")
        assertThat(query.macroses.size, equalTo(0))

        query.run { m("m1") { } }
        query.run { m("m2") { } }
        assertThat(query.macroses.size, equalTo(2))
    }

    @Test
    operator fun unaryPlus() {
        val query = Query("param")
        assertThat(query.sourceQuery, equalTo(""))

        query.run { +"select" }
        assertThat(query.sourceQuery, equalTo("select"))

        query.run { +" from" }
        assertThat(query.sourceQuery, equalTo("select from"))
    }

    @Test
    fun prepare() {
        val query = query("param") {
            +"select 1 from dual where --m1\n--m2"
        }
        assertThat(query.prepare(), equalTo("select 1 from dual where --m1\n--m2"))

        query.run {
            m("m1") {
                test({ true }) {
                    +"a=b"
                    +"c=d"
                }
            }
        }
        assertThat(query.prepare(), equalTo("select 1 from dual where (a=b) and (c=d)\n--m2"))

        query.run {
            m("m2") {
                test({ true }) {
                    +"x=y"
                }
            }
        }
        assertThat(query.prepare(), equalTo("select 1 from dual where (a=b) and (c=d)\n and (x=y)"))
    }

    @Test
    fun prepareMacroses_for_empty_macroses() {
        val query = Query(TestParam("a", "b", null))
        val result = query.prepareMacroses()
        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(0))
    }

    @Test
    fun prepareMacroses() {
        val query = Query(TestParam("a", "b", null))
        query.run {
            m("m1") {
                test({ parameter.a != null }) {
                    +"x = :a"
                }
                test({ parameter.b != null }) {
                    +"y = :b"
                }
            }
            m("m2") {
                test({ parameter.c != null }) {
                    +"z = :c"
                }
            }
        }

        val result = query.prepareMacroses()
        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(2))
        assertThat(result["m1"], CoreMatchers.allOf(containsString("x = :a"), containsString("y = :b")))
        assertThat(result["m2"], containsString("1=1"))


    }

    @Test
    fun keyToCommentRegex() {
        val regex = Query("test").keyToCommentRegex("m1")


        assertThat("select 1 from dual m1".replace(regex, ""), equalTo("select 1 from dual m1"))

        assertThat("select 1 from dual --m1".replace(regex, ""), equalTo("select 1 from dual "))
        assertThat("select 1 from dual --m1 and 5=6".replace(regex, ""), equalTo("select 1 from dual "))
        assertThat("select 1 from dual --m1 \nand 5=6".replace(regex, ""), equalTo("select 1 from dual \nand 5=6"))

        assertThat("select 1 from dual /* m1 */ and 5=6".replace(regex, ""), equalTo("select 1 from dual  and 5=6"))
    }

}

data class TestParam(val a: String, val b: String, val c: String?)