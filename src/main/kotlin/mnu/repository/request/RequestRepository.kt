package mnu.repository.request

import mnu.model.employee.Employee
import mnu.model.enums.RequestStatus
import mnu.model.request.Request
import org.springframework.data.jpa.repository.*

interface RequestRepository : JpaRepository<Request, Long>{
    fun findAllByStatus(status: RequestStatus) : List<Request>?

    fun findAllByResolver(resolver: Employee) : List<Request>?
}