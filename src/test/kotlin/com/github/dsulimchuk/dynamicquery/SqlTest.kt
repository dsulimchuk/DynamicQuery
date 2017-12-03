package com.github.dsulimchuk.dynamicquery

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.junit.After
import org.junit.Assert.assertNotNull
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
        em.transaction.begin()
    }

    @After
    fun tearDown() {
        em.transaction.commit()
        em.close()
    }

    @Test
    fun testQueryWithOneParameter() {
        val list = Sql<Long> { +"select 1 from dual where :parameter.aa.b = 6" }
                .prepare(em, 6)
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testQueryWithSeveralParameter() {
        data class SearchCriteria(val a: Long, val b: Long, val c: Long)

        val list = Sql<SearchCriteria> { +"select 1 from dual where :a = 1 and :b = 2 and :c = 3" }
                .prepare(em, SearchCriteria(1, 2, 3))
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun testQueryWithListParameter() {
        val list = Sql<List<Long>> { +"select 1 from dual where 1 in :parameter" }
                .prepare(em, listOf(1, 2, 3, 4, 5))
                .resultList as List<Long>

        assertThat(list, notNullValue())
        assertThat(list.size, equalTo(1))
    }

    @Test
    fun exampleQuery() {
        val sq = SearchCriteria(null, "viktor", 10.0, "branch_name, service_name")
        val result = query
                .prepare(em, sq)
                .resultList
        assertNotNull(result)
    }


    @Test
    fun exampleQueryWithSpecificProjection() {
        val sq = SearchCriteria(null, "viktor", 10.0, "branch_name, service_name")
        val result = query
                .prepare(em, sq, "service_name")
                .resultList as List<String>
        assertNotNull(result)
    }

    private val query = Sql<SearchCriteria> {
        +"""
select t.*
  from (select u.*
              ,s.name service_name
              ,b.name branch_name
          from users u
                 left join users_services t on (u.id = t.users_id)
                 left join services s on (t.services_id = s.id)
                 left join branches b on (s.branch_id = b.id)
         where &m1
        )t
  order by &orderMacros

"""
        projection["service_name"] = "service_name"

        //now we can declare macros m1. At runtime it will be computed on given search Criteria
        m("m1") {
            test({ parameter.id != null }) {
                +"t.id = :id"
            }
            test({ !parameter.name.isNullOrEmpty() }) {
                +"upper(s.name) like upper(:name)"
            }
            test({ parameter.salary != null }) {
                +"u.salary < :salary"
            }
        }

        //order by macros
        m("orderMacros") {
            test({ parameter.sort.isBlank() }) {
                +"name" //default sort order
            }
            test({ parameter.sort.isNotBlank() }) {
                +parameter.sort
            }
        }
    }

    data class SearchCriteria(val id: Long?,
                              val name: String?,
                              val salary: Double?,
                              val sort: String = "")

}

