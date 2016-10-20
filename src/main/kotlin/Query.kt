import java.util.*

/**
 * Created by ds on 19/10/16.
 */
class Query<T : Any>(val param: T) {
    private var queryString: String = ""
    private val testers = ArrayList<Test<T>>()
    //companion object : KLogging()


    fun print() {
        println(queryString)
    }


    fun query(query: Query<T>.() -> Unit): Unit {
        this.query()
    }

    fun test(condition: (T) -> Boolean, tester: Test<T>.() -> Unit): Test<T> {
        val test = Test<T>(condition)
        test.tester()
        testers += test
        return test
    }


    operator fun String.unaryPlus() {
        queryString += this
    }


    fun prepare(): String {
        var result = queryString ?: ""

        testers
                .filter { it.condition.invoke(param) }
                .forEach {
                    it.macroses
                            .forEach { key, value ->
                                result = result.replace(keyToCommentRegex(key), "${value.macrosText}")
                            }
                }





        return result


    }

    private fun keyToCommentRegex(key: String?) = "(-- *$key)|(/\\* *$key *\\*/)".toRegex()
    override fun toString(): String {
        return "Query(param=$param, queryString='$queryString', testers=$testers)"
    }


}

class Test<T>(val condition: (T) -> Boolean) {
    //lateinit var condition: (T) -> Boolean //= { true }
    val macroses = HashMap<String, Macros>()

    fun m1(function: Macros.() -> Unit): Macros {
        val marcos = macroses["m1"] ?: Macros("m1")
        marcos.function()
        macroses[marcos.name] = marcos
        return marcos
    }

    override fun toString(): String {
        return "Test(condition=$condition, macroses=$macroses)"
    }


}

class Macros(val name: String) {
    var macrosText: String = ""

    operator fun String.unaryPlus() {
        macrosText += " and (${this.trim()})"
    }

    override fun toString(): String {
        return "Macros(name='$name', macrosText=$macrosText)"
    }


}
