package mnu.repository

import mnu.model.Client
import mnu.model.employee.ManagerEmployee
import mnu.model.enums.ClientType
import org.springframework.data.jpa.repository.*

interface ClientRepository : JpaRepository<Client, Long> {

    fun findByEmail(email: String): Client

    fun findByUserId(id: Long): Client

    fun findAllByManager(manager: ManagerEmployee): List<Client>

    fun findAllByManagerOrderByIdAsc(manager: ManagerEmployee): List<Client>

    fun findAllByType(type: ClientType): List<Client>
}