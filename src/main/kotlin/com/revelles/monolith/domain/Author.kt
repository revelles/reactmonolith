package com.revelles.monolith.domain

import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Author.
 */
@Entity
@Table(name = "author")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Author(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "name")
    var name: String? = null,

    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,

    @OneToMany(mappedBy = "author")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    var books: MutableSet<Book> = mutableSetOf()

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addBook(book: Book): Author {
        this.books.add(book)
        book.author = this
        return this
    }

    fun removeBook(book: Book): Author {
        this.books.remove(book)
        book.author = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Author) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Author{" +
        "id=$id" +
        ", name='$name'" +
        ", birthDate='$birthDate'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
