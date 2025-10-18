package com.grupo14IngSis.snippetSearcherApp.service

// Correcto: Define el constructor principal que acepta una String y se la pasa a la clase base (RuntimeException)
class InvalidSnippetException(
    override val message: String // Debe ser val o var
) : RuntimeException(message)