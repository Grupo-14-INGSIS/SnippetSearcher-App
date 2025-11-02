package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.model.Test
import org.springframework.stereotype.Repository

@Repository
interface TestRepository {
    fun save(test: Test): Test
    fun findById(id: String): Test?
    fun findBySnippetId(snippetId: String): List<Test>
    fun update(test: Test): Test
    fun deleteById(id: String): Boolean
    fun existsById(id: String): Boolean
}