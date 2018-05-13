package com.github.dsulimchuk.dynamicquery.core

import mu.KLogging
import org.apache.commons.lang3.StringUtils
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
    val macroses = TreeMap<String, Macros<T>>(reverseOrder())
    var countAllProjection: String
        get() = projection[COUNT_ALL_PROJECTION_NAME]!!
        set(value) {
            projection[COUNT_ALL_PROJECTION_NAME] = value
        }

    val projection = HashMap<String, String>().also {
        it[COUNT_ALL_PROJECTION_NAME] = "count(*)"
    }

    fun m(macrosName: String, init: Macros<T>.() -> Unit): Macros<T> {
        val effectiveMacrosName = "&$macrosName"
        if (macroses.contains(effectiveMacrosName)) {
            throw DuplicateMacrosNameException("duplicate macros name $macrosName for $this")
        }
        val marcos = Macros<T>(effectiveMacrosName)
        marcos.init()
        macroses[effectiveMacrosName] = marcos
        return marcos
    }

    /**
     * set sourceQuery
     */
    operator fun String.unaryPlus() {
        sourceQuery += this
    }


    fun prepareText(projectionName: String? = null): String {
        val allMacroses = prepareMacroses().entries

        val searchList = allMacroses.map { it.key }.toTypedArray()
        val replacementList = allMacroses.map { it.value }.toTypedArray()
        val result = StringUtils.replaceEach(sourceQuery, searchList, replacementList)
        logger.debug { "prepareText = $result" }

        if (projectionName != null) {
            val fromTokenIndex = result.indexOf("from", 0, true)
            if (fromTokenIndex == -1) throw QueryParsingException("cannot find \"from\" token in query=$result")
            val projection = projection[projectionName]
                    ?: throw QueryParsingException("Could not find projection=$projectionName into $this")
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
        val result = it.condition(parameter)
        logger.trace { "checkCondition on $parameter with result = $result" }
        return result
    }

    override fun toString(): String {
        return "QueryDsl(parameter=$parameter, sourceQuery='$sourceQuery', macroses=$macroses, projections=$projection)"
    }
}




