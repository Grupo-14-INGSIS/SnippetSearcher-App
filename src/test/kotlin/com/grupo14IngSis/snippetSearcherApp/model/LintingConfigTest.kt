package com.grupo14IngSis.snippetSearcherApp.model

import jakarta.persistence.EntityManager
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureTestEntityManager
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import kotlin.test.assertEquals

@DataJpaTest
@ImportAutoConfiguration(exclude = [org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration::class])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@AutoConfigureTestEntityManager
class LintingConfigTest {
    @Autowired
    private lateinit var entityManager: TestEntityManager

    private val em: EntityManager
        get() = entityManager.entityManager

    @Test
    fun `should persist config with single rule and retrieve it`() {
        val rule = LintingRule("no-tabs", true, "Disallow tabs")
        val config = LintingConfig("user1", mutableListOf(rule))

        entityManager.persistAndFlush(config)

        val found = entityManager.find(LintingConfig::class.java, "user1")
        assertEquals(config.userId, found.userId)
        assertEquals(config.rules, found.rules)
    }

    @Test
    fun `should handle empty rules list`() {
        val config = LintingConfig("user2", mutableListOf())
        entityManager.persistAndFlush(config)

        val found = entityManager.find(LintingConfig::class.java, "user2")
        assertTrue(found.rules.isEmpty())
    }

    @Test
    fun `should update rule fields`() {
        val config = LintingConfig("user3", mutableListOf(LintingRule("rule", false, "init")))
        entityManager.persistAndFlush(config)

        val found = entityManager.find(LintingConfig::class.java, "user3")
        found.rules[0] = LintingRule("rule", true, "updated")
        entityManager.persistAndFlush(found)

        val reloaded = entityManager.find(LintingConfig::class.java, "user3")
        assertTrue(reloaded.rules[0].isActive)
        assertEquals("updated", reloaded.rules[0].description)
    }
}
