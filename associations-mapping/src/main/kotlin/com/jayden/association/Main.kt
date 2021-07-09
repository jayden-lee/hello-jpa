package com.jayden.association

import java.util.function.Consumer
import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

fun main() {
    val emf = Persistence.createEntityManagerFactory("jpa-study")

    process(emf) {
        saveMemberAndTeam(it)
    }

    println()

    process(emf) {
        queryLogicJoin(it)
    }

    println()

    process(emf) {
        updateRelation(it)
    }

    println()

    process(emf) {
        deleteRelation(it)
    }

    println()

    process(emf) {
        biDirection(it)
    }

    println()

    emf.close()
}

fun process(emf: EntityManagerFactory, consumer: Consumer<EntityManager>) {
    val em = emf.createEntityManager()
    val tx = em.transaction

    try {
        tx.begin()
        consumer.accept(em)
        tx.commit()
    } catch (e: Exception) {
        tx.rollback()
    } finally {
        em.close()
    }
}

fun saveMemberAndTeam(em: EntityManager) {
    println("=====saveMemberAndTeam=====")

    val team = Team(name = "team1")
    em.persist(team)

    val member1 = Member(username = "member1")
    member1.setTeam(team)
    em.persist(member1)

    val member2 = Member(username = "member2")
    member2.setTeam(team)
    em.persist(member2)
}

fun queryLogicJoin(em: EntityManager) {
    println("=====queryLogicJoin=====")

    val jpql = """
        select m from Member m join m.team t
        where t.name=:teamName
    """.trimIndent()

    val resultList = em.createQuery(jpql, Member::class.java)
        .setParameter("teamName", "team1")
        .resultList

    resultList.forEach {
        println("[query] member.username=${it.username}")
    }
}

fun updateRelation(em: EntityManager) {
    println("=====updateRelation=====")

    val team2 = Team(name = "team2")
    em.persist(team2)

    val member = em.find(Member::class.java, 1L)
    member.setTeam(team2)
}

fun deleteRelation(em: EntityManager) {
    println("=====deleteRelation=====")

    val member = em.find(Member::class.java, 1L)
    member.setTeam(null)
}

fun biDirection(em: EntityManager) {
    println("=====biDirection=====")

    val team = em.find(Team::class.java, 1L)
    team.members
        .forEach {
            println("member.username=${it.username}")
        }
}
