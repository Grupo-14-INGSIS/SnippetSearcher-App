package com.grupo14IngSis.snippetSearcherApp.repository

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class TestRepositoryImplTest {

    private lateinit var repository: TestRepositoryImpl

    @BeforeEach
    fun setup() {
        repository = TestRepositoryImpl()
    }

    @Test
    fun `save and findById`() {
        val test = com.grupo14IngSis.snippetSearcherApp.model.Test("1", "snippet1", "test1", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        repository.save(test)
        val found = repository.findById("1")
        assertEquals(test, found)
    }

    @Test
    fun `findBySnippetId`() {
        val test1 = com.grupo14IngSis.snippetSearcherApp.model.Test("1", "snippet1", "test1", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        val test2 = com.grupo14IngSis.snippetSearcherApp.model.Test("2", "snippet1", "test2", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        val test3 = com.grupo14IngSis.snippetSearcherApp.model.Test("3", "snippet2", "test3", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        repository.save(test1)
        repository.save(test2)
        repository.save(test3)
        val found = repository.findBySnippetId("snippet1")
        assertEquals(2, found.size)
        assertTrue(found.contains(test1))
        assertTrue(found.contains(test2))
    }

    @Test
    fun `update`() {
        val test = com.grupo14IngSis.snippetSearcherApp.model.Test("1", "snippet1", "test1", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        repository.save(test)
        val updatedTest = test.copy(name = "updated")
        repository.update(updatedTest)
        val found = repository.findById("1")
        assertEquals("updated", found?.name)
    }

    @Test
    fun `deleteById`() {
        val test = com.grupo14IngSis.snippetSearcherApp.model.Test("1", "snippet1", "test1", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        repository.save(test)
        assertTrue(repository.deleteById("1"))
        assertNull(repository.findById("1"))
    }
    
    @Test
    fun `existsById`() {
        val test = com.grupo14IngSis.snippetSearcherApp.model.Test("1", "snippet1", "test1", emptyList(), emptyList(), LocalDateTime.now(), LocalDateTime.now())
        repository.save(test)
        assertTrue(repository.existsById("1"))
        assertFalse(repository.existsById("2"))
    }
}