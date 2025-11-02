package com.grupo14IngSis.snippetSearcherApp.repository

import com.grupo14IngSis.snippetSearcherApp.model.Test
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class TestRepositoryImpl : TestRepository {
    private val tests = ConcurrentHashMap<String, Test>()

    override fun save(test: Test): Test {
        tests[test.id] = test
        return test
    }

    override fun findById(id: String): Test? = tests[id]

    override fun findBySnippetId(snippetId: String): List<Test> =
        tests.values.filter { it.snippetId == snippetId }

    override fun update(test: Test): Test {
        tests[test.id] = test
        return test
    }

    override fun deleteById(id: String): Boolean =
        tests.remove(id) != null

    override fun existsById(id: String): Boolean = tests.containsKey(id)
}