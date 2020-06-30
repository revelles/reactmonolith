package com.revelles.monolith.web.rest

import com.revelles.monolith.ReactmonolithApp
import com.revelles.monolith.domain.Author
import com.revelles.monolith.repository.AuthorRepository
import com.revelles.monolith.web.rest.errors.ExceptionTranslator
import java.time.LocalDate
import java.time.ZoneId
import javax.persistence.EntityManager
import kotlin.test.assertNotNull
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator

/**
 * Integration tests for the [AuthorResource] REST controller.
 *
 * @see AuthorResource
 */
@SpringBootTest(classes = [ReactmonolithApp::class])
@AutoConfigureMockMvc
@WithMockUser
class AuthorResourceIT {

    @Autowired
    private lateinit var authorRepository: AuthorRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    private lateinit var restAuthorMockMvc: MockMvc

    private lateinit var author: Author

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val authorResource = AuthorResource(authorRepository)
         this.restAuthorMockMvc = MockMvcBuilders.standaloneSetup(authorResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        author = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAuthor() {
        val databaseSizeBeforeCreate = authorRepository.findAll().size

        // Create the Author
        restAuthorMockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isCreated)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeCreate + 1)
        val testAuthor = authorList[authorList.size - 1]
        assertThat(testAuthor.name).isEqualTo(DEFAULT_NAME)
        assertThat(testAuthor.birthDate).isEqualTo(DEFAULT_BIRTH_DATE)
    }

    @Test
    @Transactional
    fun createAuthorWithExistingId() {
        val databaseSizeBeforeCreate = authorRepository.findAll().size

        // Create the Author with an existing ID
        author.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restAuthorMockMvc.perform(
            post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAuthors() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        // Get all the authorList
        restAuthorMockMvc.perform(get("/api/authors?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(author.id?.toInt())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME)))
            .andExpect(jsonPath("$.[*].birthDate").value(hasItem(DEFAULT_BIRTH_DATE.toString()))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val id = author.id
        assertNotNull(id)

        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(author.id?.toInt()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME))
            .andExpect(jsonPath("$.birthDate").value(DEFAULT_BIRTH_DATE.toString())) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingAuthor() {
        // Get the author
        restAuthorMockMvc.perform(get("/api/authors/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeUpdate = authorRepository.findAll().size

        // Update the author
        val id = author.id
        assertNotNull(id)
        val updatedAuthor = authorRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedAuthor are not directly saved in db
        em.detach(updatedAuthor)
        updatedAuthor.name = UPDATED_NAME
        updatedAuthor.birthDate = UPDATED_BIRTH_DATE

        restAuthorMockMvc.perform(
            put("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedAuthor))
        ).andExpect(status().isOk)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
        val testAuthor = authorList[authorList.size - 1]
        assertThat(testAuthor.name).isEqualTo(UPDATED_NAME)
        assertThat(testAuthor.birthDate).isEqualTo(UPDATED_BIRTH_DATE)
    }

    @Test
    @Transactional
    fun updateNonExistingAuthor() {
        val databaseSizeBeforeUpdate = authorRepository.findAll().size

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAuthorMockMvc.perform(
            put("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(author))
        ).andExpect(status().isBadRequest)

        // Validate the Author in the database
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteAuthor() {
        // Initialize the database
        authorRepository.saveAndFlush(author)

        val databaseSizeBeforeDelete = authorRepository.findAll().size

        // Delete the author
        restAuthorMockMvc.perform(
            delete("/api/authors/{id}", author.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val authorList = authorRepository.findAll()
        assertThat(authorList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_NAME = "AAAAAAAAAA"
        private const val UPDATED_NAME = "BBBBBBBBBB"

        private val DEFAULT_BIRTH_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_BIRTH_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Author {
            val author = Author(
                name = DEFAULT_NAME,
                birthDate = DEFAULT_BIRTH_DATE
            )

            return author
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Author {
            val author = Author(
                name = UPDATED_NAME,
                birthDate = UPDATED_BIRTH_DATE
            )

            return author
        }
    }
}
