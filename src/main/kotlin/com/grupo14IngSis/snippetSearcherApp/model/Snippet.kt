package com.grupo14IngSis.snippetSearcherApp.model

import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator

import org.hibernate.annotations.UuidGenerator

@Entity
data class Snippet(
    @Id @GeneratedValue
    @UuidGenerator
    val id: String? = null,
    var name: String,
    var description: String,
    var language: String,
    var version: String,
    @Lob
    var content: String
)
