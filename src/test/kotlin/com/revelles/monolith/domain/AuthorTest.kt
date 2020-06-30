package com.revelles.monolith.domain

import com.revelles.monolith.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AuthorTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Author::class)
        val author1 = Author()
        author1.id = 1L
        val author2 = Author()
        author2.id = author1.id
        assertThat(author1).isEqualTo(author2)
        author2.id = 2L
        assertThat(author1).isNotEqualTo(author2)
        author1.id = null
        assertThat(author1).isNotEqualTo(author2)
    }
}
