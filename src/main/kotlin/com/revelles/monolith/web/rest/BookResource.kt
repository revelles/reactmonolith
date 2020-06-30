package com.revelles.monolith.web.rest

import com.revelles.monolith.domain.Book
import com.revelles.monolith.repository.BookRepository
import com.revelles.monolith.web.rest.errors.BadRequestAlertException
import io.github.jhipster.web.util.HeaderUtil
import io.github.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

private const val ENTITY_NAME = "book"
/**
 * REST controller for managing [com.revelles.monolith.domain.Book].
 */
@RestController
@RequestMapping("/api")
@Transactional
class BookResource(
    private val bookRepository: BookRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /books` : Create a new book.
     *
     * @param book the book to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new book, or with status `400 (Bad Request)` if the book has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/books")
    fun createBook(@RequestBody book: Book): ResponseEntity<Book> {
        log.debug("REST request to save Book : {}", book)
        if (book.id != null) {
            throw BadRequestAlertException(
                "A new book cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = bookRepository.save(book)
        return ResponseEntity.created(URI("/api/books/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /books` : Updates an existing book.
     *
     * @param book the book to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated book,
     * or with status `400 (Bad Request)` if the book is not valid,
     * or with status `500 (Internal Server Error)` if the book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/books")
    fun updateBook(@RequestBody book: Book): ResponseEntity<Book> {
        log.debug("REST request to update Book : {}", book)
        if (book.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = bookRepository.save(book)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     book.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /books` : get all the books.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of books in body.
     */
    @GetMapping("/books")
    fun getAllBooks(): MutableList<Book> {
        log.debug("REST request to get all Books")
                return bookRepository.findAll()
    }

    /**
     * `GET  /books/:id` : get the "id" book.
     *
     * @param id the id of the book to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the book, or with status `404 (Not Found)`.
     */
    @GetMapping("/books/{id}")
    fun getBook(@PathVariable id: Long): ResponseEntity<Book> {
        log.debug("REST request to get Book : {}", id)
        val book = bookRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(book)
    }
    /**
     *  `DELETE  /books/:id` : delete the "id" book.
     *
     * @param id the id of the book to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/books/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Book : {}", id)

        bookRepository.deleteById(id)
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
