package mnu.model.employee

import mnu.model.DistrictIncident
import mnu.model.Transport
import mnu.model.Weapon
import javax.persistence.*

@Entity
@Table(name = "security_employees")
data class SecurityEmployee (var position: String? = null) {
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var employee: Employee? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(name = "security_employees_weapons",
        joinColumns = [JoinColumn(name = "employee_id")],
        inverseJoinColumns = [JoinColumn(name = "weap_id")]
    )
    var weapons: List<Weapon>? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL])
    @JoinTable(name = "security_employees_transport",
        joinColumns = [JoinColumn(name = "employee_id")],
        inverseJoinColumns = [JoinColumn(name = "tran_id")]
    )
    var transport: List<Transport>? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistants")
    var incidents: List<DistrictIncident>? = null
}