package com.grupo14IngSis.snippetSearcherApp.dto

/**
 * Alta de un snippet. Desde la UI el dueño sale siempre del token, así que [userId] se ignora; solo lo
 * usa el registro interno que hace el Runner. Con [content], App le pide al Runner que lo valide y lo guarde.
 */
data class SnippetCreationRequest(
    val userId: String = "",
    val name: String,
    val language: String,
    val description: String? = "",
    val version: String? = "1.1",
    val content: String? = null,
)
