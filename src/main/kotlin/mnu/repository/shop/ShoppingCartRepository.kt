package mnu.repository.shop

import mnu.model.Client
import mnu.model.enums.ShoppingCartStatus
import mnu.model.shop.ShoppingCart
import org.springframework.data.jpa.repository.*

interface ShoppingCartRepository : JpaRepository<ShoppingCart, Long> {
    fun findAllByClient(client: Client) : List<ShoppingCart>?

    fun findAllByClientAndStatus(client: Client, status: ShoppingCartStatus) : List<ShoppingCart>?
}