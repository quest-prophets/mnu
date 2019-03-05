package mnu.model

import mnu.model.enums.Gender
import mnu.model.enums.PersonStatus
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*

@Entity
@Table (name = "employees")
data class Employee (@Column(nullable = false) var name: String = "",
                     var password: String = "",
                     @Enumerated(EnumType.STRING) var gender: Gender = Gender.MALE,
                     var dateOfBirth: LocalDateTime? = null,
                     var dateOfEmployment: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    var status: PersonStatus? = null

    @Min(0)
    var level: Int? = null

    @Min(0)
    var salary: Long? = null

}