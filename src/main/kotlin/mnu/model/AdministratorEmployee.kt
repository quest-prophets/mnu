package mnu.model

import javax.persistence.*

@Entity
@Table (name = "administrators")
class AdministratorEmployee {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    var employee: Employee? = null


}