# Dynamic Query DSL for JPA (Hibernate) on Kotlin

This project intends to simplify maintenance of sql queries
What if you want to write a query that can be developed in another IDE (db specific)
and have opportunity to add dynamic predicates?
Now you can do it - just look:

``` kotlin
   --first of all we declare a function that accept searchCriteria (any data class)
   --inside query we can place a special placeholders (starts with &) 
   --that later will be replaced with actual computed macroses
   
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
         where 1 = 1
           and &m1
         order by b.name
        )t left join departments d on (t.department_id = d.department_id)
  where 1=1
  --&m2

"""
            --now we can declare macros m1. At runtime it will be computed on given search Criteria
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
        }
    }
```    
todo add result
