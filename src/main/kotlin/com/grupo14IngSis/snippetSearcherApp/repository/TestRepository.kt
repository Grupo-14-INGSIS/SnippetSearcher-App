package com.grupo14IngSis.snippetSearcherApp.repository

import org.springframework.stereotype.Repository
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import org.springframework.data.jpa.repository.JpaRepository

@Repository
interface TestRepository : JpaRepository<Snippet, String> {
}

