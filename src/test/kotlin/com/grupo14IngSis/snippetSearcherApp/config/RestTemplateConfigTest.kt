package com.grupo14IngSis.snippetSearcherApp.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.RestTemplate

@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [RestTemplateConfig::class])
class RestTemplateConfigTest {
    @Autowired
    private lateinit var context: ApplicationContext

    @Test
    fun `restTemplate bean should be created`() {
        val restTemplate = context.getBean(RestTemplate::class.java)
        assertNotNull(restTemplate)
    }
}
