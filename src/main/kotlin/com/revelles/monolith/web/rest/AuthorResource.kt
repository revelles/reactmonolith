package com.revelles.monolith.web.rest

import com.revelles.monolith.domain.Author
import com.revelles.monolith.repository.AuthorRepository
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

private const val ENTITY_NAME = "author"
/**
 * REST controller for managing [com.revelles.monolith.domain.Author].
 */
@RestController
@RequestMapping("/api")
@Transactional
class AuthorResource(
    private val authorRepository: AuthorRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /authors` : Create a new author.
     *
     * @param author the author to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new author, or with status `400 (Bad Request)` if the author has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/authors")
    fun createAuthor(@RequestBody author: Author): ResponseEntity<Author> {
        log.debug("REST request to save Author : {}", author)
        if (author.id != null) {
            throw BadRequestAlertException(
                "A new author cannot already have an ID",
                ENTITY_NAME, "idexists"
            )
        }
        val result = authorRepository.save(author)
        return ResponseEntity.created(URI("/api/authors/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * `PUT  /authors` : Updates an existing author.
     *
     * @param author the author to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated author,
     * or with status `400 (Bad Request)` if the author is not valid,
     * or with status `500 (Internal Server Error)` if the author couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/authors")
    fun updateAuthor(@RequestBody author: Author): ResponseEntity<Author> {
        log.debug("REST request to update Author : {}", author)
        if (author.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        val result = authorRepository.save(author)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName, true, ENTITY_NAME,
                     author.id.toString()
                )
            )
            .body(result)
    }
    /**
     * `GET  /authors` : get all the authors.
     *

     * @return the [ResponseEntity] with status `200 (OK)` and the list of authors in body.
     */
    @GetMapping("/authors")
    fun getAllAuthors(): MutableList<Author> {
        log.debug("REST request to get all Authors")
                return authorRepository.findAll()
    }

    /**
     * `GET  /authors/:id` : get the "id" author.
     *
     * @param id the id of the author to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the author, or with status `404 (Not Found)`.
     */
    @GetMapping("/authors/{id}")
    fun getAuthor(@PathVariable id: Long): ResponseEntity<Author> {
        log.debug("REST request to get Author : {}", id)
        val author = authorRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(author)
    }
    /**
     *  `DELETE  /authors/:id` : delete the "id" author.
     *
     * @param id the id of the author to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/authors/{id}")
    fun deleteAuthor(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Author : {}", id)

        authorRepository.deleteById(id)
            return ResponseEntity.noContent()
                .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
