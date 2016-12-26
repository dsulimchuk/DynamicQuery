package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.testmodel.Service
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
        em.transaction.begin()
    }

    @After
    fun tearDown() {
        em.transaction.commit()
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
               and &macros
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

    @Test
    fun testQueryWithNestedParameter() {
        data class C(val id: Long?)
        data class B(val id: Long?, val c: C?)
        data class A(val id: Long?, val b: B?)

        val list = Hql<A>(em) {
            +"select s from services s where s.id = :b_c_id"
        }
                .prepare(A(0, B(1, C(5))))
                .resultList as List<Service>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testQueryWithSubquery() {
        val userQuery = Hql<String>(em) {
            +"select s from users s where &m1"
            m("m1") {
                test({ parameter != null }) {
                    +"upper(s.name) like :parameter"
                }
            }
        }


        val services = Hql<String>(em) {
            +"select s from services s where &macros1"
            m("macros1") {
                test({ parameter != null }) {
                    +"s.id in ("
                    +userQuery.prepare("dima")
                    +")"
                }
            }
        }


        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }
}