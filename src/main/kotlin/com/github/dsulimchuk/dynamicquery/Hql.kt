package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.core.QueryDsl
import mu.KLogging
import org.apache.commons.beanutils.PropertyUtils
import javax.persistence.EntityManager
import javax.persistence.Query

/**
 * @author Dmitrii Sulimchuk
 * created 26/10/16
 */
class Hql<T : Any>(val entityManager: EntityManager,
                   val initQueryDsl: QueryDsl<T>.() -> Unit) : AbstractDialect() {
    companion object : KLogging()

    fun prepare(parameter: T): Query {
        val dsl = QueryDsl(parameter)
        dsl.initQueryDsl()

        return prepareResult(dsl)
    }

    private fun prepareResult(queryDsl: QueryDsl<T>): Query {

        val queryText = queryDsl.prepareText()
        val result = entityManager.createQuery(queryText)
        val allParameters = result.parameters?.map { it.name }?.toList() ?: emptyList()

        if (allParameters.size == 1 && isBaseType(queryDsl.parameter)) {
            logger.debug { "set parameter ${allParameters.first()} to ${queryDsl.parameter}" }
            result.setParameter(allParameters.first(), queryDsl.parameter)
        } else {
            allParameters
                    .map { it to PropertyUtils.getProperty(queryDsl.parameter, it.replace("_", ".")) }
                    .forEach {
                        logger.debug { "set parameter ${it.first} to ${it.second}" }
                        result.setParameter(it.first, it.second)
                    }
        }

        return result
    }
}
