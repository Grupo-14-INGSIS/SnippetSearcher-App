package com.grupo14IngSis.snippetSearcherApp.repository

class SnippetRepository {
    fun findByOwnerId(ownerId: String): List<Snippet>
}