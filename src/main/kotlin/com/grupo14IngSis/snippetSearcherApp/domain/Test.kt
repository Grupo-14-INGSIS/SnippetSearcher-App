package com.grupo14IngSis.snippetSearcherApp.domain

import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapKeyColumn
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
    // Variables de entorno del test: una fila por variable en test_environment (antes, JSONB en test.config_rules)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "test_environment", joinColumns = [JoinColumn(name = "id_test")])
    @MapKeyColumn(name = "env_name")
    @Column(name = "env_value", columnDefinition = "text")
    val environment: Map<String, String> = emptyMap(),
)
