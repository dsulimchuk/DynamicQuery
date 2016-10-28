package com.ds.query.core

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


    fun prepareText(): String {
        var result = sourceQuery
        val preparedMacroses = prepareMacroses()

        //replace placeholder to macros value
        preparedMacroses.forEach {
            result = result.replace(keyToCommentRegex(it.key), it.value)
        }

        //cleanup possible duplicates
        val replace = result
                .replace(Regex("where +and", IGNORE_CASE), "where")
                .replace(Regex("and +and", IGNORE_CASE), "and")

        logger.debug { "prepareText = $replace" }

        return replace
    }

    internal fun prepareMacroses(): Map<String, String> {
        return macroses
                .map {
                    val macrosText = it.value.testers.asSequence()
                            .filter { checkContition(it) }
                            .map { it.macrosText }
                            .joinToString(" ")
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
     *  where --m1
     *    and /*m2*/
     *
     * where --m1 and  /*m2*/ is a valid placeholders
     */
    internal fun keyToCommentRegex(key: String?) = "(-- *$key[^\n]*)|(/\\* *$key *\\*/)".toRegex()

    override fun toString(): String {
        return "QueryDsl(parameter=$parameter, sourceQuery='$sourceQuery', macroses=$macroses)"
    }
}

fun <T : Any> query(param: T, init: QueryDsl<T>.() -> Unit): QueryDsl<T> {
    val root = QueryDsl(param)
    root.init()
    return root
}