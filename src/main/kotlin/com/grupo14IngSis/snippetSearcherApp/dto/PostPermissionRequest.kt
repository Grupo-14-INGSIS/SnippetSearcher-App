package com.grupo14IngSis.snippetSearcherApp.dto

data class PostPermissionRequest(
  val userId: String,
  val snippetId: String,
  val role: String
)
