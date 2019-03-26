package mnu.model.shop

import mnu.model.*
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shopping_cart_items")
data class ShoppingCartItem (var dateOfCreation: LocalDateTime = LocalDateTime.now(),

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "weapon_id", referencedColumnName = "id")
                             var weapon: Weapon? = null,
                             var weaponQuantity: Long? = null,

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "transport_id", referencedColumnName = "id")
                             var transport: Transport? = null,
                             var transportQuantity: Long? = null) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    var cart: ShoppingCart? = null
}