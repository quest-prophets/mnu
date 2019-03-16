package mnu.repository.request

import mnu.model.Client
import mnu.model.request.NewVacancyRequest
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface NewVacancyRequestRepository :JpaRepository<NewVacancyRequest, Long> {
    fun findAllByClient(client: Client) : List<NewVacancyRequest>

    @Query("select r.id, nvr.requester_id, nvr.name, nvr.type, nvr.description, nvr.quantity, nvr.required_access_lvl from new_vacancy_requests nvr" +
            " inner join requests r on (nvr.request_id = r.id)" +
            " inner join clients cl on (nvr.requester_id = cl.id)" +
            " where ((cl.manager_id = :man) and (r.status = 'PENDING'))")
    fun getAllPendingRequestsForManagerByClient(@Param("man") managerId: Long) : List<Array<Any>>
}