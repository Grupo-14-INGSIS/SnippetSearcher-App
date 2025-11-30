package com.grupo14IngSis.snippetSearcherApp.dto

data class GetPermissionResponse (
  val userId: String,
  val snippetId: String,
  val role: String
)