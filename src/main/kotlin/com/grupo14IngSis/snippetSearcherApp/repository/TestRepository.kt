package com.grupo14IngSis.snippetSearcherApp.repository

import org.springframework.stereotype.Repository
import com.grupo14IngSis.snippetSearcherApp.domain.Test
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TestRepository : JpaRepository<Test, String> {

  @Transactional
  fun deleteBySnippetId(snippetId: String)

  fun findTestIdsBySnippetId(snippetId: String): List<String>

  fun findBySnippetId(snippetId: String): List<Test>

}

