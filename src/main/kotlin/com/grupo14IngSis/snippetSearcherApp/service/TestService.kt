// package com.grupo14IngSis.snippetSearcherApp.service
//
// import com.grupo14IngSis.snippetSearcherApp.dto.CreateTestRequest
// import com.grupo14IngSis.snippetSearcherApp.dto.TestResponse
// import com.grupo14IngSis.snippetSearcherApp.dto.TestResultResponse
// import com.grupo14IngSis.snippetSearcherApp.dto.UpdateTestRequest
// import com.grupo14IngSis.snippetSearcherApp.model.Test
// import com.grupo14IngSis.snippetSearcherApp.repository.TestRepository
// import com.grupo14IngSis.snippetSearcherApp.client.RunnerClient
// import org.springframework.stereotype.Service
// import java.time.LocalDateTime
// import java.util.UUID
//
// @Service
// class TestService(
//    private val testRepository: TestRepository,
//    private val snippetService: SnippetService,
//    private val runnerClient: RunnerClient
// ) {
//
//    fun createTest(snippetId: String, request: CreateTestRequest): TestResponse {
//        // Verificar que el snippet existe
//        try {
//            snippetService.getSnippet(snippetId)
//        } catch (e: Exception) {
//            throw TestException("Snippet not found: $snippetId")
//        }
//
//        val test = Test(
//            id = UUID.randomUUID().toString(),
//            snippetId = snippetId,
//            name = request.name,
//            inputs = request.inputs,
//            expectedOutputs = request.expectedOutputs
//        )
//
//        val saved = testRepository.save(test)
//        return saved.toResponse()
//    }
//
//    fun getTestsBySnippet(snippetId: String): List<TestResponse> {
//        return testRepository.findBySnippetId(snippetId)
//            .map { it.toResponse() }
//    }
//
//    fun getTestById(testId: String): TestResponse {
//        val test = testRepository.findById(testId)
//            ?: throw TestException("Test not found: $testId")
//        return test.toResponse()
//    }
//
//    fun updateTest(testId: String, request: UpdateTestRequest): TestResponse {
//        val existing = testRepository.findById(testId)
//            ?: throw TestException("Test not found: $testId")
//
//        val updated = existing.copy(
//            name = request.name ?: existing.name,
//            inputs = request.inputs ?: existing.inputs,
//            expectedOutputs = request.expectedOutputs ?: existing.expectedOutputs,
//            updatedAt = LocalDateTime.now()
//        )
//
//        val saved = testRepository.update(updated)
//        return saved.toResponse()
//    }
//
//    fun deleteTest(testId: String) {
//        if (!testRepository.deleteById(testId)) {
//            throw TestException("Test not found: $testId")
//        }
//    }
//
//    fun runTest(testId: String): TestResultResponse {
//        val test = testRepository.findById(testId)
//            ?: throw TestException("Test not found: $testId")
//
//        val snippet = try {
//            snippetService.getSnippet(test.snippetId)
//        } catch (e: Exception) {
//            throw TestException("Snippet not found: ${test.snippetId}")
//        }
//
//        return try {
//            // Ejecutar el snippet con los inputs
//            val actualOutputs = runnerClient.executeSnippet(
//                content = snippet.content,
//                inputs = test.inputs
//            )
//
//            // Comparar outputs en orden
//            val passed = actualOutputs.size == test.expectedOutputs.size &&
//                    actualOutputs.zip(test.expectedOutputs).all { (actual, expected) ->
//                        actual.trim() == expected.trim()
//                    }
//
//            TestResultResponse(
//                testId = test.id,
//                passed = passed,
//                actualOutputs = actualOutputs,
//                expectedOutputs = test.expectedOutputs,
//                isValid = passed,
//                error = null
//            )
//        } catch (e: Exception) {
//            TestResultResponse(
//                testId = test.id,
//                passed = false,
//                actualOutputs = emptyList(),
//                expectedOutputs = test.expectedOutputs,
//                isValid = false,
//                error = e.message ?: "Unknown error"
//            )
//        }
//    }
//
//    fun runAllTests(snippetId: String): List<TestResultResponse> {
//        val tests = testRepository.findBySnippetId(snippetId)
//        return tests.map { runTest(it.id) }
//    }
//
//    private fun Test.toResponse() = TestResponse(
//        id = id,
//        snippetId = snippetId,
//        name = name,
//        inputs = inputs,
//        expectedOutputs = expectedOutputs,
//        createdAt = createdAt.toString(),
//        updatedAt = updatedAt.toString()
//    )
// }
//
// class TestException(message: String) : RuntimeException(message)
