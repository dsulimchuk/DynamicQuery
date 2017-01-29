package com.github.dsulimchuk.dynamicquery.testmodel

import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

/**
 * @author Dmitrii Sulimchuk
 * created 29/01/17
 */
@Entity(name = "branches")
class Branch {
    @Id
    @GeneratedValue
    var id: Long? = null

    val name: String? = null
}