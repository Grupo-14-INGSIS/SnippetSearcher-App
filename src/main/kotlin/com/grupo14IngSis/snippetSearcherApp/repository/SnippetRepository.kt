package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface SnippetRepository : JpaRepository<Snippet, String> {
    @Transactional
    fun deleteBySnippetId(snippetId: String)

    fun findByCompliantIsNull(): List<Snippet>

    /** Borra el resultado de linteo de todos los snippets: vuelven a "pending" y el reconciliador los re-encola. */
    @Transactional
    @Modifying
    @Query("UPDATE Snippet s SET s.compliant = NULL")
    fun resetAllCompliance(): Int
}
