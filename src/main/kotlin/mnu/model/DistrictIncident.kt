package mnu.model

import mnu.model.employee.SecurityEmployee
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*

@Entity
@Table(name = "district_incidents")
data class DistrictIncident (@Min(0) @Max(3) var dangerLevel: Short = 0,

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "house_id", referencedColumnName = "id")
                             var house: DistrictHouse? = null,

                             var description: String = "",
                             var appearanceTime: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Min(1)
    @Max(10)
    var levelFrom: Int = 0

    @Min(1)
    @Max(10)
    var levelTo: Int = 0

    @Min(0)
    var availablePlaces: Long = 0

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(name = "security_in_incidents",
        joinColumns = [JoinColumn(name = "incident_id")],
        inverseJoinColumns = [JoinColumn(name = "security_id")])
    var assistants: MutableList<SecurityEmployee>? = null

}