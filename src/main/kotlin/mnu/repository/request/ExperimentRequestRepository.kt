package mnu.repository.request

import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import mnu.model.request.ExperimentRequest
import org.springframework.data.jpa.repository.*
import java.time.LocalDateTime

interface ExperimentRequestRepository : JpaRepository<ExperimentRequest, Long>{
    fun findAllByExaminator(examinator: ScientistEmployee) : List<ExperimentRequest>

    fun findAllByType(type: ExperimentType) : List<ExperimentRequest>

    @Query("select r.id as id, er.title as title, er.type as type, er.description as description, er.date as date from experiment_requests er" +
            " inner join requests r on (er.request_id = r.id)" +
            " where (r.status = 'PENDING');", nativeQuery = true)
    fun getAllPendingRequests() : List<PendingRequest>

    interface PendingRequest { val id: Long; val title: String; val type: ExperimentType; val description: String; val date: LocalDateTime }
}