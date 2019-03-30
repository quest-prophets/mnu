package mnu.model.employee

import javax.persistence.*
import javax.persistence.MapsId
import javax.persistence.FetchType



@Entity
@Table (name = "administrators")
class AdministratorEmployee {
    @Id
    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private val employee: Employee? = null
}