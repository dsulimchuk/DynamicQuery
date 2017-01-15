package com.github.dsulimchuk.dynamicquery

/**
 * @author Dmitrii Sulimchuk
 * created 15/01/2017.
 */
data class QueryResult<T>(val offset: Int?, val limit: Int?, val countAll: Long, val result: List<T>)