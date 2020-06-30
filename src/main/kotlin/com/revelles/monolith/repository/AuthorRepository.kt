package com.revelles.monolith.repository

import com.revelles.monolith.domain.Author
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data  repository for the [Author] entity.
 */
@Suppress("unused")
@Repository
interface AuthorRepository : JpaRepository<Author, Long>
