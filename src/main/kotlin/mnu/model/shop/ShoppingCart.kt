package mnu.model.shop

import mnu.model.Client
import mnu.model.enums.ShoppingCartStatus
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "shopping_carts")
data class ShoppingCart (@ManyToOne(fetch = FetchType.EAGER)
                         @JoinColumn(name = "client_id", referencedColumnName = "user_id")
                         var client: Client? = null,

                         var dateOfCreation: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    var status: ShoppingCartStatus? = null

    @OneToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL], mappedBy = "cart")
    var items: MutableList<ShoppingCartItem>? = null
}