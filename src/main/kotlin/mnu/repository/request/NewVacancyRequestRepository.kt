package mnu.repository.request

import mnu.model.Client
import mnu.model.request.NewVacancyRequest
import org.springframework.data.jpa.repository.*

interface NewVacancyRequestRepository :JpaRepository<NewVacancyRequest, Long> {
  //  fun findAllByClient(client: Client) : List<NewVacancyRequest>
/*
    @Query("select r.id, nvr.requester_id, nvr.title, nvr.salary, nvr.required_karma, nvr.vacant_places, nvr.required_karma from new_vacancy_requests nvr" +
            " inner join requests r on (nvr.request_id = r.id)  where (r.status = 'PENDING')")
    fun getAllPendingRequests() : List<Array<Any>>
*/}