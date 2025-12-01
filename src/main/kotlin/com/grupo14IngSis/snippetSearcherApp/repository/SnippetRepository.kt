package com.grupo14IngSis.snippetSearcherAccessManager.repository

import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import jakarta.transaction.Transactional
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface SnippetRepository : JpaRepository<Snippet, String> {

  @Modifying
  @Query("UPDATE Snippet s SET s.linterApplied = :state WHERE s.snippetId = :snippetId")
  fun setLintingState(snippetId: String, state: Boolean)

  @Modifying
  @Query("UPDATE Snippet s SET s.formatterApplied = :state WHERE s.snippetId = :snippetId")
  fun setFormattingState(snippetId: String, state: Boolean)
}
