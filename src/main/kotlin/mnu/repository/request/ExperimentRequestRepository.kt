package mnu.repository.request

import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import mnu.model.request.ExperimentRequest
import org.springframework.data.jpa.repository.*

interface ExperimentRequestRepository : JpaRepository<ExperimentRequest, Long>{
 //   fun findAllByExaminator(examinator: ScientistEmployee) : List<ExperimentRequest>

    fun findAllByType(type: ExperimentType) : List<ExperimentRequest>

//    @Query("select r.id, er.title, er.type, er.description, er.date from experiment_requests er" +
//            " inner join requests r on (er.request_id = r.id)" +
//            " where (r.status = 'PENDING')")
//    fun getAllPendingRequests() : List<Array<Any>>
}