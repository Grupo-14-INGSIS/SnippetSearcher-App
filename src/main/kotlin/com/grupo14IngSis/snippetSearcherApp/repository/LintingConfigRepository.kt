package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.model.LintingConfig
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LintingConfigRepository : JpaRepository<LintingConfig, String> {
    fun findByUserId(userId: Int): LintingConfig?
}
