package com.github.dsulimchuk.dynamicquery.core

import mu.KLogging
import java.util.*
import kotlin.text.RegexOption.IGNORE_CASE

/**
 * @author Dmitrii Sulimchuk
 * created 19/10/16
 */
class QueryDsl<T : Any> {
    companion object : KLogging()

    val parameter: T
    var sourceQuery: String = ""
    val macroses = HashMap<String, Macros<T>>()

    internal constructor(parameter: T) {
        this.parameter = parameter
    }

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


    fun prepareText(): QueryData {
        var queryText = sourceQuery
        val macroses = prepareMacroses()

        //replace placeholder to macros value
        macroses.forEach {
            queryText = queryText.replace(keyToCommentRegex(it.key), it.value.macrosText)
        }

        //cleanup possible duplicates
        val queryTextAfterCleanup = queryText
                .replace(Regex("where +and", IGNORE_CASE), "where")
                .replace(Regex("and +and", IGNORE_CASE), "and")

        logger.debug { "preparedQueryText = $queryTextAfterCleanup" }

        return QueryData(queryTextAfterCleanup, macroses.flatMap { it.value.macrosHints })
    }

    internal fun prepareMacroses(): Map<String, PassedMacrosData> {
        return macroses
                .map { macros ->
                    val passedTests = macros.value.testers.filter { checkContition(it) }

                    val macrosHints = passedTests.flatMap { it.queryHints }
                    val macrosText = passedTests
                            .map { it.macrosText }
                            .joinToString(" ")
                            .trim()
                            .let { if (it.isBlank()) "(1=1)" else it }

                    macros.key to PassedMacrosData(macrosText, macrosHints)
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