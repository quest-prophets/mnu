package mnu.model.request

import mnu.model.Client
import mnu.model.Transport
import mnu.model.Weapon
import javax.persistence.*

@Entity
@Table (name = "purchase_requests")
data class PurchaseRequest (@ManyToOne(fetch = FetchType.EAGER)
                            @JoinColumn(name = "requester_id", referencedColumnName = "id")
                            var client: Client? = null,

                            @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
                            @JoinTable(name = "weapons_in_purchases",
                                joinColumns = [JoinColumn(name = "purchase_id")],
                                inverseJoinColumns = [JoinColumn(name = "weapon_id")]
                            )
                            var weapons: List<Weapon>? = null,

                            @ManyToMany(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
                            @JoinTable(name = "transport_in_purchases",
                                joinColumns = [JoinColumn(name = "purchase_id")],
                                inverseJoinColumns = [JoinColumn(name = "transport_id")]
                            )
                            var transport: List<Transport>? = null) {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "request_id", referencedColumnName = "id")
    var request: Request? = null
}