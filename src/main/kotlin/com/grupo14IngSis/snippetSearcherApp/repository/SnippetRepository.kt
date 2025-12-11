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

    @Modifying
    @Query("UPDATE Snippet s SET s.linterApplied = :state WHERE s.snippetId = :snippetId")
    fun setLintingState(
        snippetId: String,
        state: Boolean,
    )

    @Modifying
    @Query("UPDATE Snippet s SET s.formatterApplied = :state WHERE s.snippetId = :snippetId")
    fun setFormattingState(
        snippetId: String,
        state: Boolean,
    )
}
