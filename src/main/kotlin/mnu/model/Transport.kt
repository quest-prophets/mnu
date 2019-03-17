package mnu.model

import mnu.model.enums.TransportType
import javax.persistence.*
import javax.validation.constraints.Min

@Entity
@Table(name = "transport")
data class Transport (@Column(nullable = false) var name: String = "",
                      @Enumerated(EnumType.STRING) var type: TransportType = TransportType.LAND,
                      var description: String = "",
                      @Min(0) var price: Double = 0.0,
                      var requiredAccessLvl: Short = 0){

    @Id
    @GeneratedValue
    var id: Long? = null

    @Min(0)
    var quantity: Long? = null
}