package com.jayden.persistence

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "Member")
class Member(
    @Id
    @Column(name = "ID")
    var id: String,

    @Column(name = "NAME")
    val username: String,

    var age: Int
)
