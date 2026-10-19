package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface SnippetRepository : JpaRepository<Snippet, String> {
    @Transactional
    fun deleteBySnippetId(snippetId: String)

    fun findByCompliantIsNull(): List<Snippet>
}
