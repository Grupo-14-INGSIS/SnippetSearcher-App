package com.grupo14IngSis.snippetSearcherApp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "snippet")
data class Snippet(
    @Id
    @Column(name = "id_snippet")
    val snippetId: String,
    @Column(nullable = false)
    val language: String,
    @Column(nullable = false)
    val bucketId: String,
    @Column(name = "formatter_applied")
    var formatterApplied: Boolean = true,
    @Column(name = "linter_applied")
    var linterApplied: Boolean = true,
)
