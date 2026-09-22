package com.grupo14IngSis.snippetSearcherApp.dto

data class SnippetPermissionData(
    val name: String,
    val language: String,
    val permission: String,
    val compliance: String? = "pending",
    val status: String? = "pending",
)
