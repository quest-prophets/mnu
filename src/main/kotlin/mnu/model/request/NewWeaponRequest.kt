package mnu.model.request

import mnu.model.Client
import mnu.model.User
import mnu.model.enums.WeaponType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table (name = "new_weapon_requests")
data class NewWeaponRequest (@Column(nullable = false) var name: String = "",
                             @Enumerated(EnumType.STRING) var type: WeaponType = WeaponType.PISTOL,
                             var description: String = "",
                             @Min(1) var quantity: Long = 1,
                             var requiredAccessLvl: Int = 0,
                             @Min(0) var price: Double = 0.0,

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "requester_id", referencedColumnName = "id")
                             var user: User? = null) {

    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null
}