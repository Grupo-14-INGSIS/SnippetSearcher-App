package com.grupo14IngSis.snippetSearcherApp.model

import jakarta.persistence.*

@Entity
@Table(name = "linting_config")
data class LintingConfig(
    @Id
    val userId: String,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "linting_rules",
        joinColumns = [JoinColumn(name = "user_id")]
    )
    val rules: MutableList<LintingRule> = mutableListOf()
)

@Embeddable
data class LintingRule(
    val name: String,
    val isActive: Boolean,
    val description: String? = null
)