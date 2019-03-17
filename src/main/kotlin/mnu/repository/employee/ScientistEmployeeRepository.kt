package mnu.repository.employee

import mnu.model.employee.ScientistEmployee
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface ScientistEmployeeRepository : JpaRepository<ScientistEmployee, Long> {
    @Query(value = "select e.id, e.name, e.level, s.position, e.salary from employee e " +
            "inner join scientists s on (e.id = s.employee_id) where (e.status = 'WORKING')")
    fun getAllWorkingScientists() : List<Array<Any>>

    @Query(value = "select count(*) from scientists s inner join requests r on (s.employee_id = r.resolver_id)" +
            " inner join experiment_requests er on (r.id = er.request_id)" +
            " where (r.status = 'RESOLVED');", nativeQuery = true)
    fun allResolvedExperimentRequests() : Long

    @Query("select s.employee_id, e.name, s.position, e.level from scientists s" +
            " inner join employees e on (s.employee_id = e.id) where (e.level < :level)")
    fun getAssistants(@Param("level") examinatorLvl: Long) : List<Array<Any>>

    @Query("select count(*) from scientists s inner join experiments ex on (s.id = ex.examinator_id);", nativeQuery = true)
    fun allConductedExperiments() : Long

    @Query("select count(*) from scientists s inner join assistants_in_experiments aie on (s.id = aie.assistant_id);", nativeQuery = true)
    fun allAssistedExperiments() : Long

}