package mnu.model.employee

import javax.persistence.*
import javax.persistence.MapsId
import javax.persistence.FetchType

@Entity
@Table (name = "administrators")
class AdministratorEmployee {
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var employee: Employee? = null
}