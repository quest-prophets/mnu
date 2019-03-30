package mnu.model.employee

import mnu.model.enums.PersonStatus
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*
import mnu.model.User
import javax.persistence.MapsId
import javax.persistence.FetchType


@Entity
@Table (name = "employees")
data class Employee (@Column(nullable = false) var name: String = "",
                     var dateOfEmployment: LocalDateTime? = null) {
    @Id
    var userId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    val user: User? = null

    @Enumerated(EnumType.STRING)
    var status: PersonStatus? = null

    @Min(0)
    @Max(10)
    var level: Int? = null

    @Min(0)
    var salary: Long? = null
}