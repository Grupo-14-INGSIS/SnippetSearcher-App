package com.grupo14IngSis.snippetSearcherApp.controller

import com.grupo14IngSis.snippetSearcherApp.config.SecurityConfig
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationRequest
import com.grupo14IngSis.snippetSearcherApp.dto.SnippetCreationResponse
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(InternalSnippetController::class)
@Import(SecurityConfig::class)
class InternalSnippetControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean(enforceOverride = false)
    private lateinit var snippetController: SnippetController

    @MockitoBean(enforceOverride = false)
    private lateinit var jwtDecoder: JwtDecoder

    @Test
    fun `the Runner registers a snippet without a token and the body userId is the owner`() {
        `when`(snippetController.registerSnippet("s1", "owner1", request()))
            .thenReturn(ResponseEntity.ok(SnippetCreationResponse(true, "ok")))

        mockMvc
            .perform(
                put("/internal/snippets/s1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"userId":"owner1","name":"n","language":"printscript"}"""),
            ).andExpect(status().isOk)

        verify(snippetController).registerSnippet("s1", "owner1", request())
    }

    @Test
    fun `a registration without userId is rejected`() {
        mockMvc
            .perform(
                put("/internal/snippets/s1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""{"name":"n","language":"printscript"}"""),
            ).andExpect(status().isBadRequest)

        verifyNoInteractions(snippetController)
    }

    private fun request() = SnippetCreationRequest(userId = "owner1", name = "n", language = "printscript")
}
