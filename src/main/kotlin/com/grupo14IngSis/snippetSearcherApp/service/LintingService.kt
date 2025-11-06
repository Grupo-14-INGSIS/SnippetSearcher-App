package com.grupo14IngSis.snippetSearcherApp.service

import com.grupo14IngSis.snippetSearcherApp.client.AccessManagerClient
import com.grupo14IngSis.snippetSearcherApp.dto.UpdateLintingRulesRequest
import com.grupo14IngSis.snippetSearcherApp.model.LintingConfig
import com.grupo14IngSis.snippetSearcherApp.model.LintingRule
import com.grupo14IngSis.snippetSearcherApp.repository.LintingConfigRepository
import org.springframework.stereotype.Service

@Service
class LintingService(
    private val lintingConfigRepository: LintingConfigRepository,
    private val accessManagerClient: AccessManagerClient
) {

    fun getUserLintingConfig(token: String): LintingConfig{
        val userId = accessManagerClient.authorize(token)
        val config = lintingConfigRepository.findByUserId(userId) // el userId tiene que se un int
            ?: createDefaultConfig(userId)

        return LintingConfig(
            userId = config.userId,
            rules = config.rules.map { toLintingRuleDTO(it) } as MutableList<LintingRule>
        )
    }

    fun updateLintingRules(token: String, request: UpdateLintingRulesRequest): LintingConfig {
        val userId = accessManagerClient.authorize(token)

        // Validar que solo se usen reglas predeterminadas
        validatePredefinedRules(request.rules)

        val config = lintingConfigRepository.findByUserId(userId)
            ?: LintingConfig(userId = userId)

        // Actualizar reglas
        config.rules.clear()
        config.rules.addAll(request.rules.map { toLintingRule(it) })

        val savedConfig = lintingConfigRepository.save(config)

        return LintingConfig(
            userId = savedConfig.userId,
            rules = savedConfig.rules.map { toLintingRuleDTO(it) } as MutableList<LintingRule>
        )
    }

    fun enableRule(token: String, ruleName: String): LintingConfig {
        val userId = accessManagerClient.authorize(token)
        val config = lintingConfigRepository.findByUserId(userId)
            ?: createDefaultConfig(userId)

        val rule = config.rules.find { it.name == ruleName }
            ?: throw IllegalArgumentException("Rule not found: $ruleName")

        config.rules.remove(rule)
        config.rules.add(rule.copy(isActive = true))

        val savedConfig = lintingConfigRepository.save(config)

        return LintingConfig(
            userId = savedConfig.userId,
            rules = savedConfig.rules.map { toLintingRuleDTO(it) } as MutableList<LintingRule>
        )
    }

    fun disableRule(token: String, ruleName: String): LintingConfig {
        val userId = accessManagerClient.authorize(token)
        val config = lintingConfigRepository.findByUserId(userId)
            ?: createDefaultConfig(userId)

        val rule = config.rules.find { it.name == ruleName }
            ?: throw IllegalArgumentException("Rule not found: $ruleName")

        config.rules.remove(rule)
        config.rules.add(rule.copy(isActive = false))

        val savedConfig = lintingConfigRepository.save(config)

        return LintingConfig(
            userId = savedConfig.userId,
            rules = savedConfig.rules.map { toLintingRuleDTO(it) } as MutableList<LintingRule>
        )
    }

    fun getPredefinedRules(): List<LintingRule> {
        return listOf(
            LintingRule(
                name = "identifier-format",
                isActive = true,
                description = "Enforces camelCase or snake_case for identifiers"
            ),
            LintingRule(
                name = "mandatory-variable-or-literal-in-println",
                isActive = true,
                description = "Requires println to have at least one variable or literal"
            ),
            LintingRule(
                name = "mandatory-variable-or-literal-in-readInput",
                isActive = true,
                description = "Requires readInput to have a string literal parameter"
            ),
            LintingRule(
                name = "no-expression-in-println",
                isActive = false,
                description = "Prevents expressions inside println statements"
            ),
            LintingRule(
                name = "no-expression-in-readInput",
                isActive = false,
                description = "Prevents expressions inside readInput statements"
            )
        )
    }

    private fun createDefaultConfig(userId: String): LintingConfig {
        val defaultConfig = LintingConfig(
            userId = userId,
            rules = getPredefinedRules().map { toLintingRule(it) }.toMutableList()
        )
        return lintingConfigRepository.save(defaultConfig)
    }

    private fun validatePredefinedRules(rules: List<com.grupo14IngSis.snippetSearcherApp.dto.LintingRule>) {
        val predefinedRuleNames = getPredefinedRules().map { it.name }.toSet()
        val invalidRules = rules.filter { it.name !in predefinedRuleNames }

        if (invalidRules.isNotEmpty()) {
            throw IllegalArgumentException(
                "Invalid rules: ${invalidRules.joinToString { it.name }}. " +
                        "Only predefined PrintScript rules are allowed."
            )
        }
    }

    private fun toLintingRuleDTO(rule: LintingRule): LintingRule {
        return LintingRule(
            name = rule.name,
            isActive = rule.isActive,
            description = rule.description
        )
    }

    private fun toLintingRule(dto: LintingRule): LintingRule {
        return LintingRule(
            name = dto.name,
            isActive = dto.isActive,
            description = dto.description
        )
    }
}