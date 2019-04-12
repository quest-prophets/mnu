package mnu.model.request

import mnu.model.User
import mnu.model.shop.ShoppingCart
import javax.persistence.*

@Entity
@Table (name = "purchase_requests")
data class PurchaseRequest (@ManyToOne(fetch = FetchType.EAGER)
                            @JoinColumn(name = "requester_id", referencedColumnName = "id")
                            var user: User? = null,

                            @OneToOne(fetch = FetchType.EAGER)
                            @JoinColumn(name = "cart_id", referencedColumnName = "id")
                            var cart: ShoppingCart? = null) {
    @Id
    var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var request: Request? = null
}