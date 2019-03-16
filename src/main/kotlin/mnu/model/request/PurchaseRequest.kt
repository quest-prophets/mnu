package mnu.model.request

import mnu.model.Client
import mnu.model.shop.ShoppingCart
import javax.persistence.*

@Entity
@Table (name = "purchase_requests")
data class PurchaseRequest (@ManyToOne(fetch = FetchType.EAGER)
                            @JoinColumn(name = "requester_id", referencedColumnName = "id")
                            var client: Client? = null,

                            @OneToOne(fetch = FetchType.EAGER)
                            @JoinColumn(name = "cart_id", referencedColumnName = "id")
                            var cart: ShoppingCart? = null) {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    var request: Request? = null
}