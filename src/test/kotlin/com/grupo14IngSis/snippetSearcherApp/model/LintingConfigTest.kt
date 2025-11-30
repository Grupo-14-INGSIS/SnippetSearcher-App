package com.grupo14IngSis.snippetSearcherApp.model

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager

@DataJpaTest
class LintingConfigTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `should persist linting config`() {
        val rule = LintingRule("rule1", true, "description")
        val config = LintingConfig("user1", mutableListOf(rule))
        
        entityManager.persistAndFlush(config)
        
        val found = entityManager.find(LintingConfig::class.java, "user1")
        
        assertEquals(config.userId, found.userId)
        assertEquals(config.rules, found.rules)
    }
}