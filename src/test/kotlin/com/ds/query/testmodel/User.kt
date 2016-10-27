package com.ds.query.testmodel

import java.math.BigDecimal
import java.util.*
import javax.persistence.*

/**
 * @author Dmitrii Sulimchuk
 * created 27/10/16
 */
@Entity(name = "users")
class User {
    @Id
    @GeneratedValue
    var id: Long? = null

    val name: String? = null

    val salary: BigDecimal? = null

    @ManyToMany
    val services: MutableList<Service> = ArrayList()
}