package com.grupo14IngSis.snippetSearcherApp.jobs

import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
import com.grupo14IngSis.snippetSearcherApp.repository.SnippetRepository
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations

class ParserVersionWatcherTest {
    private val runner = mock(RunnerClient::class.java)
    private val repository = mock(SnippetRepository::class.java)
    private val lintStatus = mock(LintStatusStore::class.java)
    private val redis = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val values = mock(ValueOperations::class.java) as ValueOperations<String, String>
    private val watcher = ParserVersionWatcher(runner, repository, lintStatus, redis)
    private val key = "printscript:parser-version"

    init {
        `when`(redis.opsForValue()).thenReturn(values)
    }

    @Test
    fun `a new parser version marks every snippet for re-evaluation`() {
        `when`(runner.getParserVersion()).thenReturn("2.1.0")
        `when`(values.getAndSet(key, "2.1.0")).thenReturn("2.0.12")

        assertTrue(watcher.checkForNewVersion())

        verify(repository).resetAllCompliance()
        verify(lintStatus).clearFailed()
    }

    @Test
    fun `the same version changes nothing`() {
        `when`(runner.getParserVersion()).thenReturn("2.0.12")
        `when`(values.getAndSet(key, "2.0.12")).thenReturn("2.0.12")

        assertFalse(watcher.checkForNewVersion())

        verify(repository, never()).resetAllCompliance()
    }

    @Test
    fun `the first version seen is only stored as the baseline`() {
        `when`(runner.getParserVersion()).thenReturn("2.0.12")
        `when`(values.getAndSet(key, "2.0.12")).thenReturn(null)

        assertFalse(watcher.checkForNewVersion())

        verify(repository, never()).resetAllCompliance()
    }

    @Test
    fun `an unreachable Runner or an unknown version changes nothing`() {
        `when`(runner.getParserVersion()).thenThrow(RuntimeException("runner down"))
        assertFalse(watcher.checkForNewVersion())

        doReturn("unknown").`when`(runner).getParserVersion()
        assertFalse(watcher.checkForNewVersion())

        verify(values, never()).getAndSet(anyString(), anyString())
        verify(repository, never()).resetAllCompliance()
    }
}
