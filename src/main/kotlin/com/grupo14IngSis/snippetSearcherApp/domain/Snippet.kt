package com.grupo14IngSis.snippetSearcherApp.domain

import jakarta.persistence.*

@Entity
@Table(name = "snippet")
data class Snippet (
    @Id
    @Column(name = "id_snippet")
    val snippetId: String,

    @Column(nullable = false)
    val bucketId: String,

    @Column(name = "formatter_applied")
    var formatterApplied: Boolean = true,

    @Column(name = "linter_applied")
    var linterApplied: Boolean = true,
)