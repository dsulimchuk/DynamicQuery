package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.core.QueryDsl
import mu.KLogging
import org.apache.commons.beanutils.PropertyUtils
import javax.persistence.EntityManager
import javax.persistence.Query
import javax.persistence.TypedQuery

/**
 * @author Dmitrii Sulimchuk
 * created 26/10/16
 */
class Hql<T : Any, R : Any>(val initQueryDsl: QueryDsl<T>.() -> Unit) : AbstractDialect() {
    companion object : KLogging()

    /**
     * Prepare untyped query
     */
    fun prepare(em: EntityManager, parameter: T): Query {
        val dsl = makeDsl(parameter)

        return em
                .createQuery(dsl.prepareText())
                .setAllQueryParameters(dsl)
    }

    /**
     * Prepare typed query
     */
    fun prepareTyped(em: EntityManager, resultClass: Class<R>, parameter: T): TypedQuery<R> {
        val dsl = makeDsl(parameter)

        return em
                .createQuery(dsl.prepareText(), resultClass)
                .setAllQueryParameters(dsl)
    }

    /**
     * execute count all query and then data query
     * if (offset ?: 0 == 0 && limit == null) -> count all query not executed
     * if (limit == 0 || countQuery return 0) -> data query not executed
     * @return paged data with total rows count
     */
    fun execute(em: EntityManager,
                resultClass: Class<R>,
                parameter: T,
                offset: Int? = null,
                limit: Int? = null,
                additionalParams: (query: TypedQuery<R>) -> Unit = { }
    ): QueryResult<R> {
        val dsl = makeDsl(parameter)

        val total: Long? = selectTotal(dsl, em, limit, offset)
        val data = selectData(dsl, em, offset, limit, total, resultClass, additionalParams)

        return QueryResult(offset, limit, total ?: data.size.toLong(), data)
    }

    //return null if there is no sense to do a query
    private fun selectTotal(dsl: QueryDsl<T>, em: EntityManager, limit: Int?, offset: Int?): Long? {
        if (offset ?: 0 == 0 && limit == null) return null

        return em.createQuery(dsl.prepareText(true))
                .setAllQueryParameters(dsl)
                .singleResult as Long
    }

    private fun selectData(dsl: QueryDsl<T>,
                           em: EntityManager,
                           offset: Int?,
                           limit: Int?,
                           total: Long?,
                           resultClass: Class<R>,
                           additionalParams: (query: TypedQuery<R>) -> Unit): List<R> {
        //if we already know that there are no results -> exit
        if (total == 0L || limit == 0) return emptyList()

        val query = em
                .createQuery(dsl.prepareText(), resultClass)
                .setAllQueryParameters(dsl)

        additionalParams(query)

        if (offset != null) {
            query.firstResult = offset
        }

        if (limit != null) {
            query.maxResults = limit
        }

        return query.resultList
    }

    private fun Hql<T, R>.makeDsl(parameter: T): QueryDsl<T> {
        val dsl = QueryDsl(parameter)
        dsl.initQueryDsl()
        return dsl
    }

    private fun <R : Query> R.setAllQueryParameters(dsl: QueryDsl<T>): R {
        val allParameters = this.parameters?.map { it.name } ?: emptyList()

        if (allParameters.size == 1 && isBaseType(dsl.parameter)) {
            logger.debug { "set parameter ${allParameters.first()} to ${dsl.parameter}" }
            this.setParameter(allParameters.first(), dsl.parameter)
        } else {
            allParameters
                    .map { it to PropertyUtils.getProperty(dsl.parameter, it.replace("_", ".")) }
                    .forEach {
                        logger.debug { "set parameter ${it.first} to ${it.second}" }
                        this.setParameter(it.first, it.second)
                    }
        }
        return this
    }
}