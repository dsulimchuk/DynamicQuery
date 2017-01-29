package com.github.dsulimchuk.dynamicquery

import com.github.dsulimchuk.dynamicquery.testmodel.Service
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hibernate.annotations.QueryHints
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
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
        val findUserServices = Hql<String, Service> {
            +"""
            select distinct t
              from services t join t.users u
             where upper(u.name) like upper(:param1||'%')
               and u.salary > 10
"""
        }

        val result = findUserServices.prepareTyped(em, Service::class.java, "user").resultList

        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(3))
    }


    @Test
    fun testQueryWithSeveralParameter() {
        data class SearchCriteria(val name: String?, val salary: Long?)

        val findUserServices = Hql<SearchCriteria, Service> {
            +"""
            select distinct t
              from services t join t.users u
             where upper(u.name) like upper(:name||'%')
               and &macros
"""
            m("macros") {
                test({ parameter.salary != null }) {
                    +"salary > :salary"
                    +"1 = 1"
                }
            }
        }


        val result = findUserServices.prepare(em, SearchCriteria("us", 20))
                .resultList

        assertThat(result, notNullValue())
        assertThat(result.size, equalTo(3))

        val result2 = findUserServices.prepare(em, SearchCriteria("us", null))
                .resultList

        assertThat(result2, notNullValue())
        assertThat(result2.size, equalTo(4))

    }

    @Test
    fun testQueryWithListParameter() {
        val list = Hql<List<Long>, Service> {
            +"select s from services s where s.id in :parameter"
        }
                .prepare(em, listOf(1, 2, 3, 5))
                .resultList

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(4))
    }

    @Test
    fun testQueryWithNestedParameter() {
        data class C(val id: Long?)
        data class B(val id: Long?, val c: C?)
        data class A(val id: Long?, val b: B?)

        val list = Hql<A, Service> {
            +"select s from services s where s.id = :b_c_id"
        }
                .prepare(em, A(0, B(1, C(5))))
                .resultList

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testTypedQueryWithListParameter() {
        val dsl = Hql<List<Long>, Service> {
            +"select s from services s where s.id in :parameter"
        }

        val list = dsl.prepareTyped(em, Service::class.java, listOf(1, 2, 3, 5)).resultList

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(4))
    }


    @Test
    fun testPagedQueryWithListParameter() {
        val dsl = Hql<List<Long>, Service> {
            +"select s from services s join s.users u where s.id in :parameter"
            countAllProjection = "count(distinct s)"
        }

        val result: QueryResult<Service> = dsl.execute(em,
                resultClass = Service::class.java,
                parameter = listOf(1, 2, 3, 5),
                offset = 1,
                limit = 2,
                additionalParams = {
                    it.setHint(QueryHints.COMMENT, "ffdf")
                    it.setHint(QueryHints.FETCH_SIZE, 10000)
                })

        assertThat(result, notNullValue())
        assertThat(result.offset, equalTo(1))
        assertThat(result.limit, equalTo(2))
        assertThat(result.countAll, equalTo(3L))
        assertThat(result.result, notNullValue())
    }

    @Test
    fun testPagedQueryWithEmptyResult() {
        val dsl = Hql<List<Long>, Service> {
            +"select s from services s join s.users u where s.id in :parameter"
            countAllProjection = "count(distinct s)"
        }

        val result: QueryResult<Service> = dsl.execute(em,
                Service::class.java,
                listOf(-1), //not exists
                1,
                2,
                {
                    it.setHint(QueryHints.COMMENT, "ffdf")
                    it.setHint(QueryHints.FETCH_SIZE, 10000)
                })

        assertThat(result, notNullValue())
        assertThat(result.offset, equalTo(1))
        assertThat(result.limit, equalTo(2))
        assertThat(result.countAll, equalTo(0L))
        assertTrue(result.result.isEmpty())
    }
}