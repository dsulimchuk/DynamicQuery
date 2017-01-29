package com.github.dsulimchuk.dynamicquery

import mu.KLogging

/**
 * @author Dmitrii Sulimchuk
 * created 26/10/16
 */

abstract class AbstractDialect {
    companion object : KLogging()

    internal fun findAllQueryParameters(queryText: String): List<String> {
        val params = Regex(":([a-zA-Z0-9_.]+)").findAll(queryText)
                .map { it.groups[1]?.value }
                .filterNotNull()
                .distinct()
                .toList()

        logger.debug { "extracted params = $params" }
        return params
    }

    protected fun isBaseType(parameter: Any): Boolean {
        return when (parameter) {
            is Float,
            is Double,
            is Int,
            is Long,
            is Short,
            is Byte,
            is Char,
            is String,
            is Iterable<*> -> true

        //todo extension
            else -> false
        }
    }
}
