package com.jayden.persistence

import javax.persistence.EntityManager
import javax.persistence.Persistence

fun main(args: Array<String>) {
    val emf = Persistence.createEntityManagerFactory("jpa-study")
    val em = emf.createEntityManager()

    val tx = em.transaction

    try {

        tx.begin()
        logic(em)
        tx.commit()

    } catch (e: Exception) {
        tx.rollback()

    } finally {
        em.close()
    }

    emf.close()
}

fun logic(em: EntityManager) {
    val memberId = "memberId"
    val member = Member(
        id = memberId,
        username = "memberName",
        age = 10
    )

    // create
    em.persist(member)

    // update
    member.age = 20

    // read one
    val findMember = em.find(Member::class.java, memberId)
    println("findMember=${findMember.username}, age=${findMember.age}")
    println()

    // read list
    val members: List<Member> = em.createQuery("select m from Member m", Member::class.java).resultList
    println("members size=${members.size}")
    println()

    // delete
    em.remove(member)
}
