package com.github.dsulimchuk.dynamicquery.core

import mu.KLogging
import java.util.*

/**
 * @author Dmitrii Sulimchuk
 * created 19/10/16
 */
class QueryDsl<T : Any>(val parameter: T) {
    companion object : KLogging()

    var sourceQuery: String = ""
    val macroses = HashMap<String, Macros<T>>()
    var countAllProjection: String = "count(*)"

    fun m(macrosName: String, init: Macros<T>.() -> Unit): Macros<T> {
        if (macroses.contains(macrosName)) {
            throw DuplicateMacrosNameException("duplicate macros name $macrosName for $this")
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
                        { query, entry -> query.replace(macrosNameToReplaceString(entry.key), entry.value) })

        logger.debug { "prepareText = $result" }

        if (forCountAll) {
            val fromTokenIndex = result.indexOf("from", 0, true)
            if (fromTokenIndex == -1) throw QueryParsingException("cannot find \"from\" token in query=$result")

            return result.replaceRange(0, fromTokenIndex, "select $countAllProjection ")
        }
        return result
    }

    internal fun prepareMacroses(): Map<String, String> {
        return macroses
                .map {
                    val suitableText: List<String> = it.value.testers
                            .filter { checkContition(it) }
                            .map { it.text() }

                    val resultingText = when (suitableText.size) {
                        0 -> "(1=1)"
                        1 -> suitableText.first().toString()
                        else -> suitableText.joinToString(
                                separator = " and ",
                                prefix = "(",
                                postfix = ")"
                        )
                    }

                    it.key to resultingText
                }.toMap()
    }

    private fun checkContition(it: Test<T>): Boolean {
        val result = it.condition(parameter)
        logger.trace { "checkContition on $parameter with result = $result" }
        return result
    }

    /**
     * regexp for replace comment with macros name
     * for example:
     * select 1
     *   from dual
     *  where 1 = 1
     *    and &m1
     *     or &m2
     *
     * where &m1 and  &m2 is a valid placeholders
     */
    internal fun macrosNameToReplaceString(key: String) = "&$key"

    override fun toString(): String {
        return "QueryDsl(parameter=$parameter, sourceQuery='$sourceQuery', macroses=$macroses)"
    }
}




