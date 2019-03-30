package mnu.model

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "district_houses"/*,
    uniqueConstraints = [UniqueConstraint(columnNames = arrayOf("shelter_row" , "shelter_column"))]*/
)
data class DistrictHouse (var shelterRow: Long = 0,
                          var shelterColumn: Long = 0,
                          var constructionDate: LocalDateTime = LocalDateTime.now()) {
    @Id
    @GeneratedValue
    var id: Long? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "districtHouse")
    var inhabitants: MutableList<Prawn>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "house")
    var incidents: MutableList<DistrictIncident>? = null
}