package mnu.repository.shop

import mnu.model.shop.ShoppingCart
import mnu.model.shop.ShoppingCartItem
import org.springframework.data.jpa.repository.*

interface ShoppingCartItemRepository : JpaRepository<ShoppingCartItem, Long> {
    fun findAllByCart(cart: ShoppingCart) : List<ShoppingCartItem>
}