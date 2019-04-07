package mnu.model.employee

import mnu.model.DistrictIncident
import mnu.model.Transport
import mnu.model.Weapon
import javax.persistence.*

@Entity
@Table(name = "security_employees")
class SecurityEmployee {
    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var employee: Employee? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "weapon_id", referencedColumnName = "id")
    var weapon: Weapon? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "transport_id", referencedColumnName = "id")
    var transport: Transport? = null

    @ManyToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "assistants")
    var incidents: List<DistrictIncident>? = null
}