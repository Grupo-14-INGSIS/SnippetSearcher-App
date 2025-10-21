package com.grupo14IngSis.snippetSearcherApp.service

class InvalidSnippetException(
    message: String,
    val rule: String,
    val line: Int,
    val column: Int,
) : RuntimeException(message)
