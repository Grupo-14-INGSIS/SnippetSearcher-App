//package com.grupo14IngSis.snippetSearcherApp.controller
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.grupo14IngSis.snippetSearcherApp.dto.LintingConfig
//import com.grupo14IngSis.snippetSearcherApp.dto.LintingRule
//import com.grupo14IngSis.snippetSearcherApp.dto.UpdateLintingRulesRequest
//import com.grupo14IngSis.snippetSearcherApp.service.LintingService
//import org.junit.jupiter.api.Test
//import org.mockito.Mockito.`when`
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//
//@WebMvcTest(LintingController::class)
//class LintingControllerTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    @MockBean
//    private lateinit var lintingService: LintingService
//
//    @Test
//    fun `should get user linting config`() {
//        val token = "Bearer test-token"
//        val configDTO = LintingConfigDTO(
//            userId = "user123",
//            rules = listOf(
//                LintingRuleDTO("identifier-format", true, "Test")
//            )
//        )
//
//        `when`(lintingService.getUserLintingConfig(token)).thenReturn(configDTO)
//
//        mockMvc.perform(get("/linting/config")
//            .header("Authorization", token))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.userId").value("user123"))
//            .andExpect(jsonPath("$.rules[0].name").value("identifier-format"))
//            .andExpect(jsonPath("$.rules[0].isActive").value(true))
//    }
//
//    @Test
//    fun `should get predefined rules`() {
//        val rules = listOf(
//            LintingRuleDTO("identifier-format", true, "Test rule 1"),
//            LintingRuleDTO("no-expression-in-println", false, "Test rule 2")
//        )
//
//        `when`(lintingService.getPredefinedRules()).thenReturn(rules)
//
//        mockMvc.perform(get("/linting/rules/predefined"))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$[0].name").value("identifier-format"))
//            .andExpect(jsonPath("$[1].name").value("no-expression-in-println"))
//    }
//
//    @Test
//    fun `should update linting rules`() {
//        val token = "Bearer test-token"
//        val request = UpdateLintingRulesRequest(
//            rules = listOf(
//                LintingRuleDTO("identifier-format", true)
//            )
//        )
//        val configDTO = LintingConfigDTO(
//            userId = "user123",
//            rules = request.rules
//        )
//
//        `when`(lintingService.updateLintingRules(token, request)).thenReturn(configDTO)
//
//        mockMvc.perform(put("/linting/config")
//            .header("Authorization", token)
//            .contentType(MediaType.APPLICATION_JSON)
//            .content(objectMapper.writeValueAsString(request)))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.userId").value("user123"))
//    }
//
//    @Test
//    fun `should enable rule`() {
//        val token = "Bearer test-token"
//        val ruleName = "identifier-format"
//        val configDTO = LintingConfigDTO(
//            userId = "user123",
//            rules = listOf(LintingRuleDTO(ruleName, true))
//        )
//
//        `when`(lintingService.enableRule(token, ruleName)).thenReturn(configDTO)
//
//        mockMvc.perform(patch("/linting/rules/$ruleName/enable")
//            .header("Authorization", token))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.rules[0].isActive").value(true))
//    }
//
//    @Test
//    fun `should disable rule`() {
//        val token = "Bearer test-token"
//        val ruleName = "identifier-format"
//        val configDTO = LintingConfigDTO(
//            userId = "user123",
//            rules = listOf(LintingRuleDTO(ruleName, false))
//        )
//
//        `when`(lintingService.disableRule(token, ruleName)).thenReturn(configDTO)
//
//        mockMvc.perform(patch("/linting/rules/$ruleName/disable")
//            .header("Authorization", token))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.rules[0].isActive").value(false))
//    }
//}