// package com.grupo14IngSis.snippetSearcherApp.controller
//
// import com.grupo14IngSis.snippetSearcherApp.dto.LintingConfig
// import com.grupo14IngSis.snippetSearcherApp.dto.UpdateLintingRulesRequest
// import com.grupo14IngSis.snippetSearcherApp.model.LintingRule
// import org.springframework.http.ResponseEntity
// import org.springframework.web.bind.annotation.*
//
// @RestController
// @RequestMapping("/linting")
// class LintingController(
//    private val lintingService: LintingService
// ) {
//
//    @GetMapping("/config")
//    fun getUserConfig(
//        @RequestHeader("Authorization") token: String
//    ): ResponseEntity<LintingConfig> {
//        val config = lintingService.getUserLintingConfig(token)
//        return ResponseEntity.ok(config)
//    }
//
//    @GetMapping("/rules/predefined")
//    fun getPredefinedRules(): ResponseEntity<List<LintingRule>> {
//        val rules = lintingService.getPredefinedRules()
//        return ResponseEntity.ok(rules)
//    }
//
//    @PutMapping("/config")
//    fun updateLintingRules(
//        @RequestHeader("Authorization") token: String,
//        @RequestBody request: UpdateLintingRulesRequest
//    ): ResponseEntity<LintingConfig> {
//        val config = lintingService.updateLintingRules(token, request)
//        return ResponseEntity.ok(config)
//    }
//
//    @PatchMapping("/rules/{ruleName}/enable")
//    fun enableRule(
//        @RequestHeader("Authorization") token: String,
//        @PathVariable ruleName: String
//    ): ResponseEntity<LintingConfig> {
//        val config = lintingService.enableRule(token, ruleName)
//        return ResponseEntity.ok(config)
//    }
//
//    @PatchMapping("/rules/{ruleName}/disable")
//    fun disableRule(
//        @RequestHeader("Authorization") token: String,
//        @PathVariable ruleName: String
//    ): ResponseEntity<LintingConfig> {
//        val config = lintingService.disableRule(token, ruleName)
//        return ResponseEntity.ok(config)
//    }
// }
