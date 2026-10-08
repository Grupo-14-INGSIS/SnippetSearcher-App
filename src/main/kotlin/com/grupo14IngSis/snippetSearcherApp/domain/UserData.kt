package com.grupo14IngSis.snippetSearcherApp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "userdata")
data class UserData(
    @Id
    @Column(name = "id_user")
    val userId: String,
    @Column(name = "name_user")
    val userName: String,
)
