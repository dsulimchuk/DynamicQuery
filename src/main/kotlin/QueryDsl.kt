import kotlin.reflect.KClass

/**
 * Created by ds on 19/10/16.
 */
object QueryDsl {
    fun <T : Any> hql(param: T, init: Query<T>.() -> Unit): Query<T> {
        val root = Query(param)
        root.init()
        return root;
    }
}


fun main(args: Array<String>) {
    println("hello!!!")
    val query = getDyncmicQuery("trara")

    println(query.prepare())

}

private fun getDyncmicQuery(s: String): Query<out Any> {
    return QueryDsl.hql(s) {
        query {
            +
            """
                select 1
                    from dual t
                   where 1 > 0
                   -- m1
                   -- m2
            """
        }

        //print(param.length)
        test({ param != null }) {
            m1 { +"1 >= :param" }
            m1 { +" 5 > 4" }
            m1 { +" 'kozel' > 'aaa'" }
        }

        test({ true }) {
            m1 { +" 1 > 0" }
        }

        /*if (!param.isNullOrEmpty()) {
            m1 { +"1 >= :param.length" }
        }
*/


        print(this)

    }

}



