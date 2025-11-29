package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetRegisterRequest(
  val snippetId: String,
  val language: String,
  val ownerId: String,
)
