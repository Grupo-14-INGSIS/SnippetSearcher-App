package com.grupo14IngSis.snippetSearcherApp.dto

data class GetPermissionsForSnippetResponse(
  val snippetId: String,
  val owner: String,
  val shared: List<String>
)