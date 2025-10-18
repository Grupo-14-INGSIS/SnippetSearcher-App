package com.grupo14IngSis.snippetSearcherApp.model

import jakarta.persistence.*

@Entity
data class Snippet(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    @Lob
    val code: String
)
