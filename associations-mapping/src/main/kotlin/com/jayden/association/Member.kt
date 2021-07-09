package com.jayden.association

import javax.persistence.*

@Entity
@Table(name = "Member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    val username: String
) {
    @ManyToOne
    @JoinColumn(name = "team_id")
    private var team: Team? = null

    fun setTeam(newTeam: Team?) {
        this.team?.members?.remove(this)
        this.team = newTeam
        this.team?.members?.add(this)
    }
}
