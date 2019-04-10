package mnu.model

import javax.persistence.*
import javax.validation.constraints.Min


@Entity
@Table(name = "vacancies")
data class Vacancy (@Column(nullable = false) var title: String = "",
                    @Min(1) var salary: Long = 0,
                    @Min(0) var requiredKarma: Long = 0,
                    @Min(0) var workHoursPerWeek: Int = 0) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "job")
    var workers: MutableList<Prawn>? = null

    @Min(0)
    var vacantPlaces: Long = 0

}