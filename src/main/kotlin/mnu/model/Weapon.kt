package mnu.model

import mnu.model.enums.WeaponType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "weapons")
data class Weapon (@Column(nullable = false) var name: String = "",
                   @Enumerated(EnumType.STRING) var type: WeaponType = WeaponType.PISTOL,
                   var description: String = "",
                   @Min(0) var price: Double = 0.0,
                   var requiredAccessLvl: Short = 0) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Min(0)
    var quantity: Long? = null

}