package com.grupo14IngSis.snippetSearcherApp.jobs

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.StringRedisTemplate

class LintStatusStoreTest {
    private val redis = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val hash = mock(HashOperations::class.java) as HashOperations<String, String, String>
    private val store = LintStatusStore(redis)
    private val key = "snippet:lint-status"

    init {
        `when`(redis.opsForHash<String, String>()).thenReturn(hash)
    }

    @Test
    fun `writes the job status in a single redis hash`() {
        store.markPending("s1")
        store.markFailed("s2")
        store.clear("s3")

        verify(hash).put(key, "s1", "pending")
        verify(hash).put(key, "s2", "failed")
        verify(hash).delete(key, "s3")
    }

    @Test
    fun `markPendingIfAbsent uses HSETNX`() {
        `when`(hash.putIfAbsent(key, "s1", "pending")).thenReturn(true)

        assertTrue(store.markPendingIfAbsent("s1"))
    }

    @Test
    fun `getAll reads every snippet with one HMGET`() {
        `when`(hash.multiGet(key, listOf("a", "b", "c"))).thenReturn(listOf("pending", null, "failed"))

        val statuses = store.getAll(listOf("a", "b", "c"))

        assertEquals(mapOf("a" to LintStatusStore.Status.PENDING, "c" to LintStatusStore.Status.FAILED), statuses)
    }

    @Test
    fun `get returns null when there is no running job`() {
        `when`(hash.multiGet(key, listOf("a"))).thenReturn(listOf(null))

        assertNull(store.get("a"))
    }

    @Test
    fun `getAll degrades to no status when redis is down`() {
        `when`(hash.multiGet(org.mockito.ArgumentMatchers.anyString() ?: key, anyList())).thenThrow(RuntimeException("down"))

        assertEquals(emptyMap<String, LintStatusStore.Status>(), store.getAll(listOf("a")))
    }

    @Test
    fun `getAll of nothing does not touch redis`() {
        assertEquals(emptyMap<String, LintStatusStore.Status>(), store.getAll(emptyList()))
    }
}
