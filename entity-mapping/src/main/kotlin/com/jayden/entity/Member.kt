package com.jayden.entity

import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "Member")
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    var id: Long,

    @Column(name = "NAME", nullable = false, length = 10)
    val username: String,

    var age: Int,

    @Enumerated(EnumType.STRING)
    val roleType: RoleType,

    @Lob
    val description: String,

    @CreationTimestamp
    val createdAt: LocalDateTime,

    @UpdateTimestamp
    val updatedAt: LocalDateTime,
)
