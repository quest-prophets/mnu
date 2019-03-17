package mnu.model.request

import mnu.model.Client
import mnu.model.enums.TransportType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table (name = "new_transport_requests")
data class NewTransportRequest (@Column(nullable = false) var name: String = "",
                             @Enumerated(EnumType.STRING) var type: TransportType = TransportType.LAND,
                             var description: String = "",
                             @Min(1) var quantity: Long = 1,
                             var requiredAccessLvl: Short = 0,

                             @ManyToOne(fetch = FetchType.EAGER)
                             @JoinColumn(name = "requester_id", referencedColumnName = "user_id")
                             var client: Client? = null)  {
    @Id
    var requestId: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val request: Request? = null

}