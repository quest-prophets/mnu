package mnu.model.shop

import mnu.model.*
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shopping_cart_items")
data class ShoppingCartItem (@ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "weapon_id", referencedColumnName = "id")
                             var weapon: Weapon? = null,

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "transport_id", referencedColumnName = "id")
                             var transport: Transport? = null,

                             var quantity: Long? = null) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cart_id", referencedColumnName = "id")
    var cart: ShoppingCart? = null

    fun name() = weapon?.name ?: transport!!.name
    fun price() = weapon?.price ?: transport!!.price
}