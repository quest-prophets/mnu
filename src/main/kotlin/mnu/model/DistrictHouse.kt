package mnu.model

import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.*

@Entity
@Table(name = "district_houses",
    uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("shelterColumn" , "shelterRow"))]
)
data class DistrictHouse (@Min(0) @Max(15) var shelterRow: Long = 0,
                          @Min(0) @Max(15) var shelterColumn: Long = 0,
                          var constructionDate: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "districtHouse")
    var inhabitants: MutableList<Prawn>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "house")
    var incidents: MutableList<DistrictIncident>? = null
}