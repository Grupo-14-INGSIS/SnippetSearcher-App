package com.grupo14IngSis.snippetSearcherApp.client

import org.springframework.stereotype.Component

@Component
class AccessManagerClient {
    fun authorize(authHeader: String): String? {
        // TODO: Implement authorization logic
        return "test-user"
    }
}
