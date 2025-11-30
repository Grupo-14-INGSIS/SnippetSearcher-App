package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GetPermissionsForUserResponseTest {

    @Test
    fun `GetPermissionsForUserResponse should have correct properties`() {
        val owned = listOf("a", "b")
        val shared = listOf("c")
        
        val response = GetPermissionsForUserResponse(
            userId = "testUser",
            owned = owned,
            shared = shared
        )

        assertEquals(owned, response.owned)
        assertEquals(shared, response.shared)
    }
}