package com.ds.query

import com.ds.query.core.Query
import mu.KLogging
import org.apache.commons.beanutils.BeanUtils
import javax.persistence.EntityManager

/**
 * @author Dmitrii Sulimchuk
 * created 23/10/16
 */
class Sql<T : Any>(val entityManager: EntityManager, val query: Query<T>) {
    companion object : KLogging()

    fun prepare(): javax.persistence.Query {
        val queryText = query.prepare()
        val result = entityManager.createNativeQuery(queryText)

        val allParameters = findAllQueryParameters(queryText)

        if (allParameters.isNotEmpty()) {
            if (allParameters.size == 1 && isBaseType(query.parameter)) {
                logger.debug { "set parameter ${allParameters[0]} to ${query.parameter}" }
                result.setParameter(allParameters[0], query.parameter)
            } else {
                allParameters
                        .map { it to BeanUtils.getProperty(query.parameter, it) }
                        .forEach {
                            logger.debug { "set parameter ${it.first} to ${it.second}" }
                            result.setParameter(it.first, it.second)
                        }
            }
        }

        return result
    }

    internal fun findAllQueryParameters(queryText: String): List<String> {
        return Regex(":([^ ]+)").findAll(queryText)
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