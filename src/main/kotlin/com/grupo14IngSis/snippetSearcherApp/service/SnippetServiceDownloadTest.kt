// package com.grupo14IngSis.snippetSearcherApp.service
//
// import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
// import com.grupo14IngSis.snippetSearcherApp.model.Snippet
// import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
// import org.junit.jupiter.api.Assertions.*
// import org.junit.jupiter.api.Test
// import org.mockito.Mockito.*
// import java.util.*
//
// class SnippetServiceDownloadTest {
//
//    private val snippetRepository = mock(SnippetRepository::class.java)
//    private val accessManagerClient = mock(AccessManagerClient::class.java)
//    private val snippetService = SnippetService(snippetRepository, accessManagerClient)
//
//    @Test
//    fun `should download original snippet successfully`() {
//        val snippetId = "123"
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val snippet = Snippet(
//            id = snippetId,
//            name = "test",
//            content = "println(\"test\")",
//            language = "kotlin",
//            author = userId
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
//
//        val result = snippetService.downloadOriginalSnippet(snippetId, token)
//
//        assertEquals(snippetId, result.id)
//        assertEquals("test", result.name)
//        assertEquals("println(\"test\")", result.content)
//        assertEquals("kotlin", result.language)
//        assertEquals("kt", result.extension)
//    }
//
//    @Test
//    fun `should throw exception when user has no permission`() {
//        val snippetId = "123"
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val snippet = Snippet(
//            id = snippetId,
//            name = "test",
//            content = "println(\"test\")",
//            language = "kotlin",
//            author = "different-user"
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//        `when`(snippetRepository.findById(snippetId)).thenReturn(Optional.of(snippet))
//
//        assertThrows(InvalidSnippetException::class.java) {
//            snippetService.downloadOriginalSnippet(snippetId, token)
//        }
//    }
// }
