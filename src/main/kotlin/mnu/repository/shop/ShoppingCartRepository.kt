package mnu.repository.shop

import mnu.model.Client
import mnu.model.User
import mnu.model.enums.ShoppingCartStatus
import mnu.model.shop.ShoppingCart
import org.springframework.data.jpa.repository.*

interface ShoppingCartRepository : JpaRepository<ShoppingCart, Long> {
    fun findAllByUser(user: User) : List<ShoppingCart>?

    fun findAllByUserAndStatus(user: User, status: ShoppingCartStatus) : List<ShoppingCart>?
}