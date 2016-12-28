package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.core.QueryData
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

        val queryData = queryDsl.prepareText()
        val jpaQuery = entityManager
                .createQuery(queryData.queryText)
                .setHintInJpaQuery(queryData)

        val allParameters = jpaQuery.parameters?.map { it.name } ?: emptyList()

        if (allParameters.size == 1 && isBaseType(queryDsl.parameter)) {
            logger.debug { "set parameter ${allParameters.first()} to ${queryDsl.parameter}" }
            jpaQuery.setParameter(allParameters.first(), queryDsl.parameter)
        } else {
            allParameters
                    .map { it to PropertyUtils.getProperty(queryDsl.parameter, it.replace("_", ".")) }
                    .forEach {
                        logger.debug { "set parameter ${it.first} to ${it.second}" }
                        jpaQuery.setParameter(it.first, it.second)
                    }
        }

        return jpaQuery
    }


}
