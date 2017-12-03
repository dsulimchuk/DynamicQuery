package com.github.dsulimchuk.dynamicquery.core

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.assertThat
import org.junit.Test

/**
 * @author Dmitrii Sulimchuk
 * created 22/10/16
 */
class QueryDslTest {

    fun <T : Any> query(param: T, init: QueryDsl<T>.() -> Unit): QueryDsl<T> {
        val root = QueryDsl(param)
        root.init()
        return root
    }

    @Test
    fun m() {
        val query = QueryDsl("param")
        assertThat(query.macroses.size, equalTo(0))

        query.run { m("m1") { } }
        query.run { m("m2") { } }
        assertThat(query.macroses.size, equalTo(2))
    }

    @Test
    operator fun unaryPlus() {
        val query = QueryDsl("param")
        assertThat(query.sourceQuery, equalTo(""))

        query.run { +"select" }
        assertThat(query.sourceQuery, equalTo("select"))

        query.run { +" from" }
        assertThat(query.sourceQuery, equalTo("select from"))
    }


    @Test
    fun prepareWithEmptyMacrosesMustBeTheSame() {
        val queryText = "select 1 from dual where 1=1 and &m1\nor &m2"
        val query = query("param") {
            +queryText
        }
        assertThat(query.prepareText(null), equalTo(queryText))
    }

    @Test
    fun prepare() {
        val query = query("param") {
            +"select 1 from dual where 1=1 and &m1\nor &m2"

            m("m1") {
                test({ true }) {
                    +"a=b"
                    +"c=d"
                }
            }
        }

        assertThat(query.prepareText(null), equalTo("select 1 from dual where 1=1 and (a=b and c=d)\nor &m2"))

        query.run {
            m("m2") {
                test({ true }) {
                    +"x=y"
                    +"1=2"
                }
            }
        }
        assertThat(query.prepareText(null), equalTo("select 1 from dual where 1=1 and (a=b and c=d)\nor (x=y and 1=2)"))
    }

    @Test(expected = QueryParsingException::class)
    fun prepareTextForCountAllNegative() {
        val query = query("param") {
            +"select 1"
        }

        query.prepareText("tratat")
    }

    @Test()
    fun prepareTextForCountAllPositiveWithDefaultProjection() {
        val query = query("param") {
            +"select 1 from dual"
        }

        val result = query.prepareText(QueryDsl.COUNT_ALL_PROJECTION_NAME)
        assertThat(result, equalTo("select count(*) from dual"))
    }


    @Test()
    fun prepareTextForCountAllPositive() {
        val query = query("param") {
            +"select 1 from services s join projects p on s.project_id = p.project_id"
            countAllProjection = "count(distict s)"
        }

        val result = query.prepareText(QueryDsl.COUNT_ALL_PROJECTION_NAME)
        assertThat(result, equalTo("select count(distict s) from services s join projects p on s.project_id = p.project_id"))
    }

    @Test
    fun prepareMacroses_for_empty_macroses() {
        val query = QueryDsl(TestParam("a", "b", null))
        val result = query.prepareMacroses()
        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(0))
    }

    @Test
    fun prepareMacroses() {
        val query = QueryDsl(TestParam("a", "b", null))
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
        assertThat(result["m1"], allOf(containsString("x = :a"), containsString("y = :b")))
        assertThat(result["m2"], containsString("1=1"))


    }

    @Test
    fun keyToCommentRegex() {
        val regex = QueryDsl("test").macrosNameToReplaceString("m1")


        assertThat("select 1 from dual where &m1".replace(regex, ""), equalTo("select 1 from dual where "))

        assertThat("select 1 from dual &m1".replace(regex, ""), equalTo("select 1 from dual "))
        assertThat("select 1 from dual &m1 \n and 5=6".replace(regex, ""),
                equalTo("select 1 from dual  \n and 5=6"))
        assertThat("select 1 from dual &m1 \nand 5=6".replace(regex, ""),
                equalTo("select 1 from dual  \nand 5=6"))
        assertThat("select 1 from dual where &m1 and 2=2".replace(regex, "1=2"),
                equalTo("select 1 from dual where 1=2 and 2=2"))

        assertThat("select 1 from dual /* &m1 */ and 5=6".replace(regex, "xxx"),
                equalTo("select 1 from dual /* xxx */ and 5=6"))
    }

    @Test(expected = DuplicateMacrosNameException::class)
    fun shouldThrowErrorIfDefineSameMacrosTwice() {
        val query = QueryDsl(TestParam("a", "b", null))

        query.run {
            m("m1") {

            }
            m("m1") {

            }
        }
    }
}

data class TestParam(val a: String?, val b: String?, val c: String?)