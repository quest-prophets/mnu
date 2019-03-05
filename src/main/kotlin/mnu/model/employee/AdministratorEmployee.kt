package mnu.model.employee

import javax.persistence.*

@Entity
@Table (name = "administrators")
class AdministratorEmployee {
    @Id
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id", referencedColumnName = "id")
    var employee: Employee? = null


}