package mnu.repository.employee

import mnu.model.employee.ScientistEmployee
import org.springframework.data.jpa.repository.*
import org.springframework.data.repository.query.Param

interface ScientistEmployeeRepository : JpaRepository<ScientistEmployee, Long> {
//    @Query(value = "select e.user_id, e.name, e.level, s.position, e.salary from employees e " +
//            "inner join scientists s on (e.user_id = s.employee_user_id) where (e.status = 'WORKING');", nativeQuery = true)
//    fun getAllWorkingScientists() : List<Array<Any>>


    @Query("select s.employee_user_id as id, e.name as name, e.position as position, e.level as level from scientists s" +
            " inner join employees e on (s.employee_user_id = e.user_id) where (e.level < ?1);", nativeQuery = true)
    fun getAssistants(examinatorLvl: Int) : List<Assistant>

    interface Assistant { val id: Long; val name: String; val position: String; val level: Int }

    @Query("select count(*) from scientists s inner join experiments ex on (s.id = ex.examinator_id);", nativeQuery = true)
    fun allConductedExperiments() : Long

    @Query("select count(*) from scientists s inner join assistants_in_experiments aie on (s.id = aie.assistant_id);", nativeQuery = true)
    fun allAssistedExperiments() : Long

}