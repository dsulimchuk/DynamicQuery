package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.core.QueryData
import mu.KLogging
import javax.persistence.Query

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

    internal fun <T : Query> T.setHintInJpaQuery(queryData: QueryData): T {
        if (queryData.hints.isNotEmpty()) {
            this.setHint("org.hibernate.tratata", queryData.hintsAsString())
        }
        return this
    }
}
