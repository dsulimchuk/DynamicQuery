package com.ds.query


import com.ds.query.core.QueryDsl
import com.ds.query.core.query
import java.util.*

/**
* @author Dmitrii Sulimchuk
* created 21/10/16
*/


fun main(args: Array<String>) {
    val query = UserRepository.getDyncmicQuery(SearchCriteria(null, "viktor", Date(), null))
    println(query.prepareText())
    QueryDsl("")
}

object UserRepository {

    fun getDyncmicQuery(searchCriteria: SearchCriteria): QueryDsl<out Any> {
        return query(searchCriteria) {
            +"""
select t.*
  from (select t.*
              ,s.name service_name
              ,b.name branch_name
          from user_services t
                 left join services s on (t.service_id = s.service_id)
                 left join branch b on (t.branch_id = b.branch_id)
         where --m
         order by b.name
        )t left join departments d on (t.department_id = d.department_id)
  where --m2

"""

            m("m1") {
                test({ parameter.id != null }) {
                    +"t.id = :id"
                }
                test({ !searchCriteria.name.isNullOrEmpty() }) {
                    +"upper(t.name) like upper(:name)"
                }
                test({ searchCriteria.activationDate != null }) {
                    +":activationDate between t.start_date and t.end_date"
                }
            }

            m("m2") {
                test({ parameter.departmentId != null }) {
                    +"t.id = :departmentId"
                }
            }

            m("m1") {

            }


        }
    }
}

data class SearchCriteria(val id: Long?, val name: String?, val activationDate: Date?, val  departmentId: Long?) {

}

