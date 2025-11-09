// package com.grupo14IngSis.snippetSearcherApp.service
//
// import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
// import com.grupo14IngSis.snippetSearcherApp.dto.LintingRule
// import com.grupo14IngSis.snippetSearcherApp.dto.UpdateLintingRulesRequest
// import com.grupo14IngSis.snippetSearcherApp.model.LintingConfig
// import com.grupo14IngSis.snippetSearcherApp.model.LintingRule
// import com.grupo14IngSis.snippetSearcherApp.repository.LintingConfigRepository
// import org.junit.jupiter.api.Assertions.*
// import org.junit.jupiter.api.Test
// import org.mockito.Mockito.*
//
// class LintingServiceTest {
//
//    private val lintingConfigRepository = mock(LintingConfigRepository::class.java)
//    private val accessManagerClient = mock(AccessManagerClient::class.java)
//    private val lintingService = LintingService(lintingConfigRepository, accessManagerClient)
//
//    @Test
//    fun `should get user linting config`() {
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val config = LintingConfig(
//            userId = userId,
//            rules = mutableListOf(
//                LintingRule("identifier-format", true, "Test rule")
//            )
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//        `when`(lintingConfigRepository.findByUserId(userId)).thenReturn(config)
//
//        val result = lintingService.getUserLintingConfig(token)
//
//        assertEquals(userId, result.userId)
//        assertEquals(1, result.rules.size)
//        assertEquals("identifier-format", result.rules[0].name)
//        assertTrue(result.rules[0].isActive)
//    }
//
//    @Test
//    fun `should enable rule successfully`() {
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val ruleName = "identifier-format"
//        val config = LintingConfig(
//            userId = userId,
//            rules = mutableListOf(
//                LintingRule(ruleName, false, "Test rule")
//            )
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//        `when`(lintingConfigRepository.findByUserId(userId)).thenReturn(config)
//        `when`(lintingConfigRepository.save(any())).thenAnswer { it.arguments[0] }
//
//        val result = lintingService.enableRule(token, ruleName)
//
//        assertTrue(result.rules.find { it.name == ruleName }?.isActive ?: false)
//    }
//
//    @Test
//    fun `should disable rule successfully`() {
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val ruleName = "identifier-format"
//        val config = LintingConfig(
//            userId = userId,
//            rules = mutableListOf(
//                LintingRule(ruleName, true, "Test rule")
//            )
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//        `when`(lintingConfigRepository.findByUserId(userId)).thenReturn(config)
//        `when`(lintingConfigRepository.save(any())).thenAnswer { it.arguments[0] }
//
//        val result = lintingService.disableRule(token, ruleName)
//
//        assertFalse(result.rules.find { it.name == ruleName }?.isActive ?: true)
//    }
//
//    @Test
//    fun `should throw exception for invalid rule`() {
//        val userId = "user123"
//        val token = "Bearer test-token"
//        val request = UpdateLintingRulesRequest(
//            rules = listOf(
//                LintingRule("invalid-rule", true, "Invalid")
//            )
//        )
//
//        `when`(accessManagerClient.validateToken(token)).thenReturn(userId)
//
//        assertThrows(IllegalArgumentException::class.java) {
//            lintingService.updateLintingRules(token, request)
//        }
//    }
//
//    @Test
//    fun `should get predefined rules`() {
//        val rules = lintingService.getPredefinedRules()
//
//        assertTrue(rules.isNotEmpty())
//        assertTrue(rules.any { it.name == "identifier-format" })
//        assertTrue(rules.any { it.name == "mandatory-variable-or-literal-in-println" })
//    }
// }
