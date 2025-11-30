package com.grupo14IngSis.snippetSearcherApp.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TaskRequestTest {

    @Test
    fun `TaskRequest should have correct properties`() {
        val userId = "uid"
        
        val request = TaskRequest(
            userId = userId
        )

        assertEquals(userId, request.userId)
    }
}