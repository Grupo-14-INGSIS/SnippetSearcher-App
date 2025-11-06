//package com.grupo14IngSis.snippetSearcherApp.controller
//
//import com.grupo14IngSis.snippetSearcherApp.dto.SnippetDownload
//import com.grupo14IngSis.snippetSearcherApp.dto.SnippetDownloadDTO
//import com.grupo14IngSis.snippetSearcherApp.service.SnippetService
//import jdk.javadoc.internal.doclets.formats.html.markup.HtmlStyle
//import org.mockito.Mockito.`when`
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.http.ResponseEntity.status
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//import reactor.core.publisher.Mono.`when`
//
//@WebMvcTest(SnippetController::class)
//class SnippetControllerDownloadTest {
//
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockBean
//    private lateinit var snippetService: SnippetService
//
//    @Test
//    fun `should download original snippet`() {
//        val snippetId = "123"
//        val token = "Bearer test-token"
//        val downloadDTO = SnippetDownloadDTO(
//            id = snippetId,
//            name = "test-snippet",
//            content = "println(\"Hello World\")",
//            language = "kotlin",
//            extension = "kt"
//        )
//
//        `when`(snippetService.downloadOriginalSnippet(snippetId, token))
//            .thenReturn(downloadDTO)
//
//        mockMvc.perform(get("/snippet/$snippetId/download/original")
//            .header("Authorization", token))
//            .andExpect(status().isOk)
//            .andExpect(
//                HtmlStyle.header().string("Content-Disposition",
//                "attachment; filename=\"test-snippet.kt\""))
//            .andExpect(content().contentType("application/octet-stream"))
//    }
//
//    @Test
//    fun `should download formatted snippet`() {
//        val snippetId = "123"
//        val token = "Bearer test-token"
//        val downloadDTO = SnippetDownload(
//            id = snippetId,
//            name = "test-snippet",
//            content = "println(\"Hello World\")",
//            language = "kotlin",
//            extension = "kt"
//        )
//
//        `when`(snippetService.downloadFormattedSnippet(snippetId, token))
//            .thenReturn(downloadDTO)
//
//        mockMvc.perform(get("/snippet/$snippetId/download/formatted")
//            .header("Authorization", token))
//            .andExpect(status().isOk)
//            .andExpect(
//                HtmlStyle.header().string("Content-Disposition",
//                "attachment; filename=\"test-snippet.kt\""))
//    }
//}