package com.grupo14IngSis.snippetSearcherApp.domain

import io.hypersistence.utils.hibernate.type.array.ListArrayType
import io.hypersistence.utils.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.Type

@Entity
@Table(name = "test")
data class Test(
    @Id
    @Column(name = "id_test")
    val testId: String,
    @Column(name = "id_snippet")
    val snippetId: String,
    @Type(ListArrayType::class)
    @Column(name = "in_put", columnDefinition = "text[]")
    val input: List<String> = emptyList(),
    @Type(ListArrayType::class)
    @Column(name = "out_put", columnDefinition = "text[]")
    val output: List<String> = emptyList(),
    @Column(name = "version", columnDefinition = "text")
    val version: String,
    @Type(JsonType::class)
    @Column(name = "config_rules", columnDefinition = "jsonb")
    val environment: Map<String, String> = emptyMap(),
)
