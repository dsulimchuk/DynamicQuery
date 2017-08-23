package com.github.dsulimchuk.dynamicquery.core

import mu.KLogging
import java.util.*

/**
 * @author Dmitrii Sulimchuk
 * created 19/10/16
 */
class QueryDsl<T : Any>(val parameter: T) {
    companion object : KLogging() {
        val COUNT_ALL_PROJECTION_NAME = "countAllProjection"
    }

    var sourceQuery: String = ""
    val macroses = HashMap<String, Macros<T>>()
    var countAllProjection: String
        get() = projection[COUNT_ALL_PROJECTION_NAME]!!
        set(value) {
            projection[COUNT_ALL_PROJECTION_NAME] = value
        }

    val projection = HashMap<String, String>().also {
        it[COUNT_ALL_PROJECTION_NAME] = "count(*)"
    }

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


    fun prepareText(projectionName: String? = null): String {
        val result = prepareMacroses()
                .asIterable()
                .fold(sourceQuery,
                        { query, entry -> query.replace(keyToCommentRegex(entry.key), entry.value) })

        logger.debug { "prepareText = $result" }

        if (projectionName != null) {
            val fromTokenIndex = result.indexOf("from", 0, true)
            if (fromTokenIndex == -1) throw QueryParsingException("cannot find \"from\" token in query=$result")
            val projection = projection[projectionName] ?: throw QueryParsingException("Could not find $projectionName into $this")
            return result.replaceRange(0, fromTokenIndex, "select ${projection} ")
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
     *    and &m1
     *     or &m2
     *
     * where &m1 and  &m2 is a valid placeholders
     */
    internal fun keyToCommentRegex(key: String?) = "(&$key)".toRegex()

    override fun toString(): String {
        return "QueryDsl(parameter=$parameter, sourceQuery='$sourceQuery', macroses=$macroses, projections=$projection)"
    }

}




