package com.ds.query

import com.ds.query.core.query
import com.ds.query.testmodel.Service
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
 * *         created 26/10/16
 */
class HqlTest {
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
        val findUserServices = Hql<String>(em) {
            +"""
            select distinct t
              from services t join t.users u
             where upper(u.name) like upper(:param1||'%')
               and u.salary > 10
"""
        }

        val result = findUserServices.prepare("user").resultList as List<Long>

        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(3))
    }


    @Test
    fun testQueryWithSeveralParameter() {
        data class SearchCriteria(val name: String?, val salary: Long?)

        val findUserServices = Hql<SearchCriteria>(em) {
            +"""
            select distinct t
              from services t join t.users u
             where upper(u.name) like upper(:name||'%')
               and --macros
"""
            m("macros") {
                test({ parameter.salary != null }) {
                    +"salary > :salary"
                }
            }
        }
        val result = findUserServices.prepare(SearchCriteria("us", 20))
                .resultList as List<Service>

        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(2))

        val result2 = findUserServices.prepare(SearchCriteria("us", null))
                .resultList as List<Service>

        assertThat(result2, notNullValue())
        assertThat(result2.size, equalTo(4))

    }

    @Test
    fun testQueryWithListParameter() {
        val list = Hql<List<Long>>(em) {
            +"select s from services s where s.id in :parameter"
        }
                .prepare(listOf(1, 2, 3, 5))
                .resultList as List<Service>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(4))
    }
}