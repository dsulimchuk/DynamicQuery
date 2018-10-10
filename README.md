# Dynamic Query DSL for JPA (Hibernate) on Kotlin

This project intends to simplify maintenance of sql queries.
What if you want to write a query that can be developed in another IDE (db specific)
and have opportunity to add dynamic predicates?
Now you can do it - just look:

``` kotlin
   //first of all we declare a function that accept searchCriteria (any data class)
   //inside query we can place a special placeholders (starts with &) 
   //that later will be replaced with actual computed macroses
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
         where 1=1
           &m1
        )t
  order by &orderMacros

"""

        //now we can declare macros m1. At runtime it will be computed on given search Criteria
        m("m1") {
            test({ parameter.id != null }) {
                +"and u.id = :id"
            }
            test({ !parameter.name.isNullOrEmpty() }) {
                +"and upper(s.name) like upper(:name)"
            }
            test({ parameter.salary != null }) {
                +"and u.salary < :salary"
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
        
        //you can specify additional projections
        projection["service_name"] = "service_name"
    }

    data class SearchCriteria(val id: Long?,
                              val name: String?,
                              val salary: Double?,
                              val sort: String = "")



```  
  
now we simply call prepare method and pass entityManager with actual parameters
``` kotlin
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
```    

## Download

```xml
<dependency>
    <groupId>com.github.dsulimchuk.dynamicquery</groupId>
    <artifactId>dynamic-query</artifactId>
    <version>1.0.6</version>
</dependency>
```
