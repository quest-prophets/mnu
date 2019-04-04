package mnu.model.employee

import mnu.model.Client
import mnu.model.Prawn
import javax.persistence.*

@Entity
@Table(name = "managers")
data class ManagerEmployee (var position: String? = null){
    @Id
    private var id: Long? = null

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    var employee: Employee? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var clients: MutableList<Client>? = null

    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "manager")
    var prawns: MutableList<Prawn>? = null
}