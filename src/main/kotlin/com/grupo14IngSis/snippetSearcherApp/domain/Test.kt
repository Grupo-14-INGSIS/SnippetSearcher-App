package com.grupo14IngSis.snippetSearcherApp.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "test")
data class Test(
    @Id
    @Column(name = "id_test")
    val testId: String,
    @Column(name = "id_snippet")
    val snippetId: String,
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "in_put", columnDefinition = "text[]")
    val input: List<String> = emptyList(),
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "out_put", columnDefinition = "text[]")
    val output: List<String> = emptyList(),
    @Column(name = "version", columnDefinition = "text")
    val version: String,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_rules", columnDefinition = "jsonb")
    val environment: Map<String, String> = emptyMap(),
)
