package com.revelles.monolith.repository

import com.revelles.monolith.domain.Book
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Book] entity.
 */
@Suppress("unused")
@Repository
interface BookRepository : JpaRepository<Book, Long>
