package com.jayden.entity

import org.hibernate.tool.schema.Action
import javax.persistence.Persistence

fun main(args: Array<String>) {
    val emf = Persistence.createEntityManagerFactory("jpa-study")
    val em = emf.createEntityManager()

    val tx = em.transaction

    try {

        tx.begin()
        tx.commit()

    } catch (e: Exception) {
        tx.rollback()

    } finally {
        em.close()
    }

    emf.close()
}