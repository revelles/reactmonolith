package com.revelles.monolith.web.rest

import com.revelles.monolith.ReactmonolithApp
import com.revelles.monolith.domain.Book
import com.revelles.monolith.repository.BookRepository
import com.revelles.monolith.web.rest.errors.ExceptionTranslator
import java.math.BigDecimal
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
 * Integration tests for the [BookResource] REST controller.
 *
 * @see BookResource
 */
@SpringBootTest(classes = [ReactmonolithApp::class])
@AutoConfigureMockMvc
@WithMockUser
class BookResourceIT {

    @Autowired
    private lateinit var bookRepository: BookRepository

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

    private lateinit var restBookMockMvc: MockMvc

    private lateinit var book: Book

    @BeforeEach
    fun setup() {
        MockitoAnnotations.initMocks(this)
        val bookResource = BookResource(bookRepository)
         this.restBookMockMvc = MockMvcBuilders.standaloneSetup(bookResource)
             .setCustomArgumentResolvers(pageableArgumentResolver)
             .setControllerAdvice(exceptionTranslator)
             .setConversionService(createFormattingConversionService())
             .setMessageConverters(jacksonMessageConverter)
             .setValidator(validator).build()
    }

    @BeforeEach
    fun initTest() {
        book = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBook() {
        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // Create the Book
        restBookMockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isCreated)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1)
        val testBook = bookList[bookList.size - 1]
        assertThat(testBook.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testBook.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testBook.publicationDate).isEqualTo(DEFAULT_PUBLICATION_DATE)
        assertThat(testBook.price).isEqualTo(DEFAULT_PRICE)
    }

    @Test
    @Transactional
    fun createBookWithExistingId() {
        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // Create the Book with an existing ID
        book.id = 1L

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc.perform(
            post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooks() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList
        restBookMockMvc.perform(get("/api/books?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].publicationDate").value(hasItem(DEFAULT_PUBLICATION_DATE.toString())))
            .andExpect(jsonPath("$.[*].price").value(hasItem(DEFAULT_PRICE?.toInt()))) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val id = book.id
        assertNotNull(id)

        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.id?.toInt()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.publicationDate").value(DEFAULT_PUBLICATION_DATE.toString()))
            .andExpect(jsonPath("$.price").value(DEFAULT_PRICE?.toInt())) }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingBook() {
        // Get the book
        restBookMockMvc.perform(get("/api/books/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun updateBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

        // Update the book
        val id = book.id
        assertNotNull(id)
        val updatedBook = bookRepository.findById(id).get()
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook)
        updatedBook.title = UPDATED_TITLE
        updatedBook.description = UPDATED_DESCRIPTION
        updatedBook.publicationDate = UPDATED_PUBLICATION_DATE
        updatedBook.price = UPDATED_PRICE

        restBookMockMvc.perform(
            put("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedBook))
        ).andExpect(status().isOk)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList[bookList.size - 1]
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.publicationDate).isEqualTo(UPDATED_PUBLICATION_DATE)
        assertThat(testBook.price).isEqualTo(UPDATED_PRICE)
    }

    @Test
    @Transactional
    fun updateNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeDelete = bookRepository.findAll().size

        // Delete the book
        restBookMockMvc.perform(
            delete("/api/books/{id}", book.id)
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private val DEFAULT_PUBLICATION_DATE: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_PUBLICATION_DATE: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_PRICE: BigDecimal = BigDecimal(1)
        private val UPDATED_PRICE: BigDecimal = BigDecimal(2)

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Book {
            val book = Book(
                title = DEFAULT_TITLE,
                description = DEFAULT_DESCRIPTION,
                publicationDate = DEFAULT_PUBLICATION_DATE,
                price = DEFAULT_PRICE
            )

            return book
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Book {
            val book = Book(
                title = UPDATED_TITLE,
                description = UPDATED_DESCRIPTION,
                publicationDate = UPDATED_PUBLICATION_DATE,
                price = UPDATED_PRICE
            )

            return book
        }
    }
}
