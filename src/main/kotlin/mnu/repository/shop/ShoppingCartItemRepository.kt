package mnu.repository.shop

import mnu.model.Transport
import mnu.model.Weapon
import mnu.model.enums.ShoppingCartStatus
import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import org.springframework.data.jpa.repository.*

interface ShoppingCartItemRepository : JpaRepository<ShoppingCartItem, Long> {
    fun findAllByCart(cart: ShoppingCart) : List<ShoppingCartItem>?

    fun findByWeaponAndCart(weapon: Weapon, cart: ShoppingCart) : ShoppingCartItem?

    fun findByTransportAndCart(transport: Transport, cart: ShoppingCart) : ShoppingCartItem?

    fun findAllByCartUserIdAndCartStatus(cartUserId: Long, status: ShoppingCartStatus): List<ShoppingCartItem>

    fun findByIdAndCartUserIdAndCartStatus(id: Long, cartUserId: Long, status: ShoppingCartStatus): ShoppingCartItem?
}