package mnu.model.employee

import mnu.model.DistrictIncident
import mnu.model.Transport
import mnu.model.Weapon
import javax.persistence.*

@Entity
@Table(name = "security_employees")
class SecurityEmployee {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
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

    var position: String? = null
}