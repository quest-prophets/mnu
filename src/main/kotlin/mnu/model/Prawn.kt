package mnu.model

import mnu.model.employee.ManagerEmployee
import mnu.model.enums.DeathReason
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "prawns")
data class Prawn (@Column(nullable = false) var name: String = "") {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    val user: User? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", referencedColumnName = "id")
    var districtHouse: DistrictHouse? = null
/*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", referencedColumnName = "employee_id")
    var manager: ManagerEmployee? = null
*/
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    var job: Vacancy? = null

    var balance: Long = 0
    var karma: Long = 0
    var dateOfDeath: LocalDateTime? = null
    var deathReason: DeathReason? = null

}