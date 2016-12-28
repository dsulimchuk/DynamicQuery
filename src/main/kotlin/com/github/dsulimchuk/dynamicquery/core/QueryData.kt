package com.github.dsulimchuk.dynamicquery.core

/**
 * @author Dmitrii Sulimchuk
 * created 28/12/16
 */
data class QueryData(val queryText: String, val hints: List<String>) {
    fun hintsAsString(): String = hints.joinToString(separator = " ")
}
