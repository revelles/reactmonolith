package com.revelles.monolith.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.math.BigDecimal
import java.time.LocalDate
import javax.persistence.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy

/**
 * A Book.
 */
@Entity
@Table(name = "book")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(name = "title")
    var title: String? = null,

    @Column(name = "description")
    var description: String? = null,

    @Column(name = "publication_date")
    var publicationDate: LocalDate? = null,

    @Column(name = "price", precision = 21, scale = 2)
    var price: BigDecimal? = null,

    @ManyToOne @JsonIgnoreProperties(value = ["books"], allowSetters = true)
    var author: Author? = null

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Book{" +
        "id=$id" +
        ", title='$title'" +
        ", description='$description'" +
        ", publicationDate='$publicationDate'" +
        ", price=$price" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
