package com.github.dsulimchuk.dynamicquery.testmodel

import java.util.*
import javax.persistence.*

/**
 * @author Dmitrii Sulimchuk
 * created 27/10/16
 */
@Entity(name = "services")
class Service {
    @Id
    @GeneratedValue
    var id: Long? = null

    var name: String? = null

    @ManyToMany(mappedBy = "services")
    var users: MutableList<User> = ArrayList()

    @ManyToOne
    var branch: Branch? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as Service

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + (name?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Service(id=$id, name=$name)"
    }

}