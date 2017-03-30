package com.github.dsulimchuk.dynamicquery.hibernate

import org.hibernate.resource.jdbc.spi.StatementInspector
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dmitrii Sulimchuk
 * *         created 30/03/2017
 */
class StatementInspectorImpl : StatementInspector {
    companion object {
        private val counter = ThreadLocal.withInitial { AtomicInteger() }

        fun queryCount() = counter.get().get()
        fun reset() = counter.get().set(0)
    }

    override fun inspect(sql: String?): String? {
        counter.get().getAndIncrement()
        return sql
    }
}