package com.ds.query

import com.ds.query.core.Query
import mu.KLogging
import org.apache.commons.beanutils.BeanUtils
import javax.persistence.EntityManager

/**
 * @author Dmitrii Sulimchuk
 * created 26/10/16
 */
class Hql<T : Any>(val entityManager: EntityManager,
                   val initQuery: Query<T>.() -> Unit)
: AbstractDialect() {
    companion object : KLogging()

    fun prepare(parameter: T): javax.persistence.Query {
        val query = Query(parameter)
        query.initQuery()

        val queryText = query.prepareText()
        val result = entityManager.createQuery(queryText)

        val allParameters = findAllQueryParameters(queryText)

        if (allParameters.isNotEmpty()) {
            if (allParameters.size == 1 && isBaseType(query.parameter)) {
                Sql.logger.debug { "set parameter ${allParameters[0]} to ${query.parameter}" }
                result.setParameter(allParameters[0], query.parameter)
            } else {
                allParameters
                        .map { it to BeanUtils.getProperty(query.parameter, it) }
                        .forEach {
                            Sql.logger.debug { "set parameter ${it.first} to ${it.second}" }
                            result.setParameter(it.first, it.second)
                        }
            }
        }

        return result
    }
}
