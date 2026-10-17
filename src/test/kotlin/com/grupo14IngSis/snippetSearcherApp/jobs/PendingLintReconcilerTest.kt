package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.domain.Snippet
import com.grupo14IngSis.snippetSearcherApp.dto.GetPermissionsForSnippetResponse
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class PendingLintReconcilerTest {
    private val repository = mock(SnippetRepository::class.java)
    private val accessManager = mock(AccessManagerClient::class.java)
    private val producer = mock(SnippetJobProducer::class.java)
    private val reconciler = PendingLintReconciler(repository, accessManager, producer)

    @Test
    fun `queues never linted snippets with the owner rules and survives errors`() {
        val first = Snippet("s1", "a", "printscript", "s1")
        val broken = Snippet("s2", "b", "printscript", "s2")
        val third = Snippet("s3", "c", "printscript", "s3")
        `when`(repository.findByCompliantIsNull()).thenReturn(listOf(first, broken, third))
        `when`(accessManager.getPermissionsForSnippet("s1")).thenReturn(GetPermissionsForSnippetResponse("s1", "o1", emptyList()))
        `when`(accessManager.getPermissionsForSnippet("s2")).thenThrow(RuntimeException("access manager down"))
        `when`(accessManager.getPermissionsForSnippet("s3")).thenReturn(GetPermissionsForSnippetResponse("s3", "o3", emptyList()))

        reconciler.reconcile()

        verify(producer).requestLintIfIdle(first, "o1")
        verify(producer).requestLintIfIdle(third, "o3")
    }
}
