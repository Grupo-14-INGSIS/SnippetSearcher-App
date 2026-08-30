package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.domain.Test
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface TestRepository : JpaRepository<Test, String> {
    @Transactional
    fun deleteBySnippetId(snippetId: String)

    @Query("SELECT t.testId FROM Test t WHERE t.snippetId = :snippetId")
    fun findTestIdsBySnippetId(@Param("snippetId") snippetId: String): List<String>

    fun findBySnippetId(snippetId: String): List<Test>
}
