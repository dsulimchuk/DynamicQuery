package com.github.dsulimchuk.dynamicquery

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

/**
 * @author Dmitrii Sulimchuk
 * *         created 23/10/16
 */
class SqlTest {
    val sessionFactory: EntityManagerFactory = Persistence.createEntityManagerFactory("test")
    val em: EntityManager = sessionFactory.createEntityManager()

    @Before
    fun setUp() {
        em.getTransaction().begin()
    }

    @After
    fun tearDown() {
        em.getTransaction().commit()
        em.close()
    }

    @Test
    fun testQueryWithOneParameter() {
        val list = Sql<Long>(em) { +"select 1 from dual where :parameter.aa.b = 6" }
                .prepare(6)
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testQueryWithSeveralParameter() {
        data class SearchCriteria(val a: Long, val b: Long, val c: Long)

        val list = Sql<SearchCriteria>(em) { +"select 1 from dual where :a = 1 and :b = 2 and :c = 3" }
                .prepare(SearchCriteria(1, 2, 3))
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testQueryWithListParameter() {
        val list = Sql<List<Long>>(em) { +"select 1 from dual where 1 in :parameter" }
                .prepare(listOf(1, 2, 3, 4, 5))
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }
}