package mnu.model

import mnu.model.employee.SecurityEmployee
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*

data class DistrictIncident (@Min(0) @Max(2) var dangerLevel: Short = 0,
                             var description: String = "",
                             var appearanceTime: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "house_id", referencedColumnName = "id")
    var house: DistrictHouse? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(name = "security_in_incidents",
        joinColumns = [JoinColumn(name = "incident_id")],
        inverseJoinColumns = [JoinColumn(name = "security_id")])
    var assistants: List<SecurityEmployee>? = null

}