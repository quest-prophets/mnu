package mnu.model.request

import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.employee.ManagerEmployee
import mnu.model.employee.SecurityEmployee
import javax.persistence.*

@Entity
@Table (name = "change_equipment_requests")
data class ChangeEquipmentRequest (@ManyToOne(fetch = FetchType.EAGER)
                                   @JoinColumn(name = "requester_id", referencedColumnName = "employee_id")
                                   var employee: SecurityEmployee? = null,

                                   @ManyToOne(fetch = FetchType.EAGER)
                                   @JoinColumn(name = "new_weapon_id", referencedColumnName = "id")
                                   var weapon: Weapon? = null,

                                   @ManyToOne(fetch = FetchType.EAGER)
                                   @JoinColumn(name = "new_transport_id", referencedColumnName = "id")
                                   var transport: Transport? = null) {
    @Id
    var requestId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val request: Request? = null
}