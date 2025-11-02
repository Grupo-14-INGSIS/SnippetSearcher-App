package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestRequest
import com.grupo14IngSis.snippetSearcherApp.dto.TestResponse
import com.grupo14IngSis.snippetSearcherApp.dto.TestResultResponse
import com.grupo14IngSis.snippetSearcherApp.dto.UpdateTestRequest
import com.grupo14IngSis.snippetSearcherApp.service.TestService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class TestController(
    private val testService: TestService
) {

    @PostMapping("/snippets/{snippetId}/tests")
    fun createTest(
        @PathVariable snippetId: String,
        @RequestBody request: CreateTestRequest
    ): ResponseEntity<TestResponse> {
        val test = testService.createTest(snippetId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(test)
    }

    @GetMapping("/snippets/{snippetId}/tests")
    fun getTestsBySnippet(
        @PathVariable snippetId: String
    ): ResponseEntity<List<TestResponse>> {
        val tests = testService.getTestsBySnippet(snippetId)
        return ResponseEntity.ok(tests)
    }

    @GetMapping("/tests/{testId}")
    fun getTest(
        @PathVariable testId: String
    ): ResponseEntity<TestResponse> {
        val test = testService.getTestById(testId)
        return ResponseEntity.ok(test)
    }

    @PutMapping("/tests/{testId}")
    fun updateTest(
        @PathVariable testId: String,
        @RequestBody request: UpdateTestRequest
    ): ResponseEntity<TestResponse> {
        val test = testService.updateTest(testId, request)
        return ResponseEntity.ok(test)
    }

    @DeleteMapping("/tests/{testId}")
    fun deleteTest(
        @PathVariable testId: String
    ): ResponseEntity<Void> {
        testService.deleteTest(testId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/tests/{testId}/run")
    fun runTest(
        @PathVariable testId: String
    ): ResponseEntity<TestResultResponse> {
        val result = testService.runTest(testId)
        return ResponseEntity.ok(result)
    }

    @PostMapping("/snippets/{snippetId}/tests/run-all")
    fun runAllTests(
        @PathVariable snippetId: String
    ): ResponseEntity<List<TestResultResponse>> {
        val results = testService.runAllTests(snippetId)
        return ResponseEntity.ok(results)
    }
}