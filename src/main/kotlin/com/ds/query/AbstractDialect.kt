package com.ds.query

/**
 * @author Dmitrii Sulimchuk
 * created 26/10/16
 */

abstract class AbstractDialect {
    internal fun findAllQueryParameters(queryText: String): List<String> {
        return Regex(":([a-zA-Z0-9_.]+)").findAll(queryText)
                .map { it.groups[1]?.value }
                .filterNotNull()
                .distinct()
                .toList()
    }

    internal fun isBaseType(parameter: Any): Boolean {
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
