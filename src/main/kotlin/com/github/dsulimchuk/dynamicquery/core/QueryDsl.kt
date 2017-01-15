package com.github.dsulimchuk.dynamicquery.core

import mu.KLogging
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * @author Dmitrii Sulimchuk
 * created 19/10/16
 */
class QueryDsl<T : Any> (val parameter: T) {
    companion object : KLogging() {
        private val whereThenAndReplaceRegex = Regex("where +and", IGNORE_CASE)
        private val doubleAndregex = Regex("and +and", IGNORE_CASE)
    }

    var sourceQuery: String = ""
    val macroses = HashMap<String, Macros<T>>()
    var countAllProjection: String = "count(*)"

    fun m(macrosName: String, init: Macros<T>.() -> Unit): Macros<T> {
        if (macroses.contains(macrosName)) {
            throw RuntimeException("duplicate macros name $macrosName for $this")
        }
        val marcos = Macros<T>(macrosName)
        marcos.init()
        macroses[macrosName] = marcos
        return marcos
    }

    /**
     * set sourceQuery
     */
    operator fun String.unaryPlus() {
        sourceQuery += this
    }


    fun prepareText(forCountAll: Boolean = false): String {
        val result = prepareMacroses()
                .asIterable()
                .fold(sourceQuery,
                        { query, entry -> query.replace(keyToCommentRegex(entry.key), entry.value) })
                //cleanup possible duplicates
                .replace(whereThenAndReplaceRegex, "where")
                .replace(doubleAndregex, "and")

        logger.debug { "prepareText = $result" }

        if (forCountAll) {
            val fromTokenIndex = result.indexOf("from", 0, true)
            if (fromTokenIndex == -1) throw QueryParsingException("cannot find \"from\" token in query=$result")

            return result.replaceRange(0, fromTokenIndex, "select ${countAllProjection} ")
        }
        return result
    }

    internal fun prepareMacroses(): Map<String, String> {
        return macroses
                .map {
                    val macrosText = it.value.testers.asSequence()
                            .filter { checkContition(it) }
                            .map { it.macrosText }
                            .joinToString(" ")
                            .trim()
                            .let { if (it.isBlank()) "(1=1)" else it }

                    it.key to macrosText
                }.toMap()
    }

    private fun checkContition(it: Test<T>): Boolean {
        val result = it.condition.invoke(parameter)
        logger.trace { "checkContition on $parameter with result = $result" }
        return result
    }

    /**
     * regexp for replace comment with macros name
     * for example:
     * select 1
     *   from dual
     *  where 1 = 1
     *    --&m1
     *    /*&m2 */
     *
     * where --&m1 and  /*&m2*/ is a valid placeholders
     */
    internal fun keyToCommentRegex(key: String?) = "(-- *&$key[^\n]*)|(/\\* *&$key *\\*/)|(&$key)".toRegex()

    override fun toString(): String {
        return "QueryDsl(parameter=$parameter, sourceQuery='$sourceQuery', macroses=$macroses)"
    }

}


fun <T : Any> query(param: T, init: QueryDsl<T>.() -> Unit): QueryDsl<T> {
    val root = QueryDsl(param)
    root.init()
    return root
}

