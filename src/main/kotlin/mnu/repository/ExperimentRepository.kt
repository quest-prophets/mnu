package mnu.repository

import mnu.model.Experiment
import mnu.model.employee.ScientistEmployee
import mnu.model.enums.ExperimentType
import org.springframework.data.jpa.repository.*

interface ExperimentRepository : JpaRepository<Experiment, Long> {
    fun findAllByType(type: ExperimentType) : List<Experiment>

 //   fun findAllByExaminator(examinator: ScientistEmployee) : List<Experiment>

   // fun findAllByExaminatorAndType(examinator: ScientistEmployee, type: ExperimentType) : List<Experiment>

//    @Query(value = "select e.* from experiments e inner join assistants_in_experiments aie on (e.id = aie.experiment_id)" +
//            " inner join scientists s on (aie.assistant_id = s.id)" +
//            " where (aie.assistant_id = ?1);", nativeQuery = true)
//    fun getAllByAssistant(assistantId: Long) : List<Experiment>
}